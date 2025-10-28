# 프로젝트 전환 요구사항 요약

- 기존 모놀리식 프로젝트 `example-shop-springboot-monolithic-mark2` 를 기반으로, 현재 경로의 각 MSA 서비스(`config`, `eureka`, `gateway`, `order`, `payment`, `product`, `user`)를 동일한 컨벤션으로 구현한다.
- 공통 로직은 GitHub 저장소 `jaybon1/example-shop-springboot-msa-global` 에서 파악하고, JitPack 배포본 `com.github.jaybon1:example-shop-springboot-msa-global:0.0.1` 을 각 서비스의 `build.gradle` 에 의존성으로 추가되어 있으니 참고한다.
- 서비스 간 통신은 Kafka 등의 이벤트 방식 대신 `RestTemplate` 기반 REST 호출로 구성한다.
- 장애 대응을 위해 Resilience4j 서킷 브레이커를 사용한다.
- 분산 추적은 Zipkin 으로 통합한다.
- 모호한 사항은 반드시 사용자에게 확인한 후 진행한다.

## MSA 전환 상세 계획 (user → product → order → payment)

### 공통 준비
1. `../example-shop-springboot-msa-global` 의 `com.example.shop.global` 패키지 구조와 DTO/예외/엔티티 베이스 클래스를 분석하여 재사용 가능한 구성 요소 목록을 작성한다.
2. 각 서비스 `build.gradle` 에 `com.github.jaybon1:example-shop-springboot-msa-global:0.0.1`, `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-cloud-starter-netflix-eureka-client`, `spring-cloud-starter-config`, `spring-cloud-starter-zipkin`, `resilience4j-spring-boot3` 등 필수 디펜던시와 Lombok, H2(MySQL 전환 전 임시) 설정을 추가한다.
3. `config` 서비스 원격 설정 repo 를 점검하여 서비스별 `application-{profile}.yml` 이 존재하는지 확인하고, 없을 경우 `datasource`, `eureka.client`, `spring.zipkin`, `resilience4j` 기본 설정 템플릿을 작성한다.
4. `gateway` 의 `application.yml` 과 라우팅 설정을 검토하여 user/product/order/payment 서비스 엔드포인트 경로와 포트를 사전 정의한다.
5. Zipkin 서버, Eureka, Config 서비스가 로컬에서 동시에 기동되는 실행 스크립트 혹은 README 절차를 마련해 서비스별 통합 테스트 환경을 표준화한다.

### 1. user 서비스 전환 (`com.example.shop.user`)
1. 모놀리식 `../example-shop-springboot-monolithic-mark2/src/main/java/com/example/shop/user` 패키지를 전수 조사하여 사용할 도메인 객체(`domain/model`), JPA 엔티티(`infrastructure/persistence/entity`), 레포지토리(`domain/repository`, `infrastructure/persistence/repository`) 목록을 정의한다.
2. `src/main/java/com/example/shop/user` 에 `domain`, `infrastructure`, `application`, `presentation` 패키지를 동일하게 구성하고, 필요 시 공통 모듈로 이동 가능한 컴포넌트(예: `UserRole`, `UserSocial`) 여부를 결정한다.
3. `application/service/AuthServiceV1`, `UserServiceV1` 를 기준으로 인증/회원 CRUD 로직을 마이크로서비스용 서비스 계층으로 이관하며, 외부 SNS 연동(`infrastructure/api/kakao`) 호출부는 Resilience4j 서킷 브레이커를 적용한 `RestTemplate` 기반 비동기/재시도 구성으로 리팩터링한다.
4. `presentation/controller` 와 `presentation/dto` 를 참고하여 API 스펙을 정의하고, 공통 응답 포맷(`ApiDto`) 을 이용하도록 변환하면서 Swagger/OpenAPI 문서화 요구가 있는지 확인한다.
5. `application.yml` 에 Eureka, Config, Zipkin, Resilience4j 기본 설정을 추가하고, 보안 기능(모놀리식 `spring-security` 설정) 분리 전략을 확정한다. JWT 발급/검증은 `user` 서비스가 계속 담당하도록 구성한다.
6. Flyway 혹은 `schema.sql` 을 이용해 user 서비스용 DB 마이그레이션 스크립트를 작성하고, 임시로 H2 메모리 DB로 로컬 통합 테스트를 구축한다.

### 2. product 서비스 전환 (`com.example.shop.product`)
1. 모놀리식 `product` 도메인의 엔티티(`ProductEntity`), 도메인 모델(`Product`), 매퍼(`ProductMapper`), 레포지토리 구현(`ProductRepositoryImpl`) 을 마이크로서비스 레이어에 맞춰 재배치하고, 공통 `BaseEntity` 사용 여부를 결정한다.
2. `ProductServiceV1` 의 재고/가격/상품 CRUD 로직을 분리하면서 user 서비스와의 연관이 필요한지(예: 상품 등록자 검증) 검토하고, 필요 시 user 서비스 호출용 `RestTemplate` 클라이언트와 Resilience4j 서킷 브레이커 구성을 추가한다.
3. `presentation/controller` 및 DTO 를 참고하여 `/api/v1/products` REST 엔드포인트와 내부 전용 `/internal/v1/products/{productId}/release-stock`, `/internal/v1/products/{productId}/return-stock` API 를 정의하고, Gateway 라우팅 정보와 일치하도록 스키마를 확정한다.
4. JPA 설정, CommandLineRunner 기반 초기 데이터 로딩(`ProductCommandLineRunner`) 을 profile 기반으로 분리하여 개발과 운영 환경 차이를 최소화한다.
5. 통합 테스트 시나리오(상품 CRUD + 재고 확인)를 `@SpringBootTest` 또는 `@DataJpaTest` 로 구성하고, Zipkin trace id 가 정상 전파되는지 확인한다.

### 3. order 서비스 전환 (`com.example.shop.order`)
1. 모놀리식 `Order`, `OrderItem` 도메인 모델과 `OrderEntity`, `OrderItemEntity`, `OrderMapper` 구조를 그대로 재현하되, 주문 생성 시 user/product 서비스에 대한 동기 호출 흐름을 명시한다.
2. `OrderServiceV1` 의 주문 생성/조회/취소 비즈니스 로직을 마이크로서비스 구조로 이관하면서 다음과 같은 외부 연동 포인트를 정의한다:  
   - 사용자 유효성 검증: user 서비스 `GET /api/v1/users/{id}`  
   - 상품 재고 차감: product 서비스 `POST /internal/v1/products/{productId}/stock-release`  
   각 호출에 대해 `RestTemplate` + Resilience4j(CircuitBreaker + Retry) 설정을 `application.yml` 에 명시한다.
3. 여러 상품에 대한 재고 차감 중 하나라도 실패하면 전체 주문을 롤백하고, 이미 차감된 재고는 `POST /internal/v1/products/{productId}/return-stock` 보상 호출로 복원하도록 트랜잭션/보상 로직을 설계한다. CircuitBreaker open 상태에서 임시 주문 생성 차단 정책도 포함한다.
4. 주문 상태 이벤트(주문 생성 시 Payment 서비스 호출 트리거)를 REST 방식으로 설계하고, `OrderControllerV1` API 스펙을 문서화한다.
5. 주문 관련 DB 마이그레이션 스크립트를 작성하고, 통합 테스트에서 user/product 서비스 Mock 서버(예: WireMock)로 연동 흐름을 검증한다.

### 4. payment 서비스 전환 (`com.example.shop.payment`)
1. 모놀리식 `Payment` 도메인, `PaymentEntity`, `PaymentRepositoryImpl` 및 `PaymentServiceV1` 로직을 분해하여 결제 승인/취소 흐름을 order 서비스와 REST 인터페이스로 재정의한다.
2. 결제 승인 시 order 서비스에 주문 상태 업데이트를 요청하기 위한 클라이언트(`RestTemplate`) 를 구현하고, Resilience4j CircuitBreaker + TimeLimiter 로 결제-주문 간 장애 전파를 차단한다.
3. 외부 결제 게이트웨이(추후 도입 가능)에 대비해 인터페이스를 추상화하고, 결제 실패/부분 취소 케이스를 테스트 케이스로 명문화한다.
4. `PaymentControllerV1` 엔드포인트와 DTO 를 재구성하여 `/api/v1/payments` 스펙을 정의하고, Gateway/Config 설정을 업데이트한다.
5. 주문 서비스와의 통합 테스트(결제 성공, 결제 실패, CircuitBreaker open) 시나리오를 작성하여 Zipkin Trace 상에서 전체 호출 체인이 추적되는지 확인한다.

### 마무리 점검
1. `config`, `eureka`, `gateway` 서비스 설정을 각 서비스 배포 전에 최신 상태로 맞추고, CI 파이프라인에서 순차 기동 스크립트를 준비한다.
2. 서비스별 `Dockerfile`, `docker-compose`(필요 시) 를 작성하여 로컬 다중 서비스 테스트 자동화를 지원한다.
3. 최종 점검 시, user → product → order → payment 순으로 Smoke Test 를 실행하고, Resilience4j 상태 모니터링 및 Zipkin 트레이스를 확인한다.

## DTO · Controller · Service 레이어 컨벤션

`example-shop-springboot-monolithic-mark2` 와 현재 MSA(user/product) 코드를 기준으로, 나머지 마이크로서비스도 동일한 규칙을 따른다.

### 공통 전제
- 패키지 구조: `presentation(컨트롤러·DTO)` → `application(service)` → `domain(model·repository)` → `infrastructure(...)` 를 유지한다.
- 버저닝: 외부 노출 클래스/DTO는 `V1` 접미사를 반드시 붙인다. 향후 버전 추가 시 동일 네이밍 패턴으로 병존시킨다.
- Lombok: DTO는 `@Getter`, `@Builder`, 필요 시 `@AllArgsConstructor` 를 사용해 불변 구조를 유지한다. 서비스/컨트롤러는 `@RequiredArgsConstructor` 로 의존성을 주입한다.
- 응답 래퍼: 컨트롤러 응답은 항상 `ApiDto<T>` 를 사용하며, `ResponseEntity.ok(ApiDto.builder()...)` 형태로 반환한다.

### DTO 레이어
- 네이밍:
  - 요청: `Req{HttpMethod}{Resource}{세부기능}DtoV1` (`ReqPostProductsDtoV1`, `ReqPostAuthRegisterDtoV1`).
  - 응답: `Res{동사/리소스}DtoV1` 또는 페이지 구조일 경우 `Res{동사/리소스}sDtoV1` (복수형은 관례적으로 `ResGetUsersDtoV1`).
- 구조:
  - 요청/응답 모두 최상위 DTO 안에 실제 payload를 나타내는 정적 중첩 클래스를 둔다. 예) `ReqPostProductsDtoV1.ProductDto`, `ResGetProductDtoV1.ProductDto`.
  - 리스트/페이지 응답은 `PagedModel<T>` 를 상속한 중첩 클래스를 사용해 page metadata 를 유지한다. (monolithic `ResGetProductsDtoV1` → `ProductPageDto`, `MSA ResGetUsersDtoV1` 동일 방식).
  - 변환은 정적 팩토리 메서드를 사용한다: `of(domain)`, `from(domain)` 혹은 `builder()` 체인을 활용한다. 예) `ResPostProductsDtoV1.of(Product)` / `ProductDto.from(Product)`.
  - 요청 DTO 는 `jakarta.validation` 애노테이션(@NotNull, @Min 등) 으로 유효성 검사를 명시한다.
  - 식별자는 문자열(UUID)로 직렬화하되, 도메인에선 `UUID` 를 사용한다. 변환 시 `uuid.toString()` 으로 처리한다.
- 응답 메시지:
  - 컨트롤러에서 메시지를 별도로 제공할 경우 `ApiDto` 의 `message` 필드를 채운다. DTO 내부에는 메시지 필드를 두지 않는다.

### Controller 레이어
- 클래스 레벨:
  - `@RestController`, `@RequestMapping("/v1/{resource}")`, `@RequiredArgsConstructor` 를 기본으로 사용한다.
  - 공통 보안 설정이 필요한 경우 `@AutoConfigureMockMvc(addFilters = false)` 등 테스트에서만 적용. 실제 컨트롤러는 필터를 그대로 둔다.
- 메서드 네이밍:
  - `HTTP Method + 경로` 형식을 따른다. 예) `GET /v1/products` → `getProducts`, `POST /internal/v1/products/{id}/release-stock` → `postInternalProductReleaseStock`.
  - 내부 전용 엔드포인트는 `/internal` prefix 를 경로에 포함시키고, 메서드명에도 `Internal`을 명시해 외부 API와 구분한다.
- 시그니처:
  - 목록 조회: `Pageable` 은 `@PageableDefault` 와 함께 첫 번째 파라미터로 두고, 필터 파라미터는 `@RequestParam(required = false)`로 뒤에 배치한다.
  - 단건 조회/삭제: `@PathVariable UUID` 파라미터를 사용하고, 인증 정보가 필요하면 `@AuthenticationPrincipal CustomUserDetails` 를 첫 번째 인자로 받는다. `CustomUserDetails` 가 `null` 일 수 있으므로 null-safe 처리 후 Service 로 전달한다.
  - 생성/수정: `@RequestBody @Valid Req...DtoV1` 를 사용하며, 서비스에는 DTO 전체를 전달한다. (예: `productServiceV1.postProducts(reqDto)`).
- 응답:
  - 성공 응답은 `ResponseEntity.ok(ApiDto.<Res...DtoV1>builder().message(...).data(...).build())`.
  - 데이터가 없는 경우 `ApiDto.builder().message(...).build()` 로 빈 데이터를 반환한다.
  - 내부 API 는 명시적인 상태 코드(보통 200)로 응답하고 `code`, `message`, `data` 를 채운다.
- 예외 처리:
  - 컨트롤러는 예외를 직접 처리하지 않고, `...presentation.advice` 패키지의 `@RestControllerAdvice` 에서 처리하도록 위임한다 (monolithic/msa 동일 패턴).

### Service 레이어
- 클래스 레벨:
  - `@Service`, `@RequiredArgsConstructor`, `@Transactional(readOnly = true)` 를 기본으로 적용한다.
  - 쓰기 작업이 있는 메서드에 `@Transactional` 을 다시 선언해 readOnly 를 해제한다.
- 메서드 시그니처/역할:
  - 컨트롤러에서 받은 인증 정보(사용자 ID, 롤 리스트 등)는 별도 DTO 로 래핑하지 않고, 원시 타입(`UUID`, `List<String>`) 으로 그대로 넘긴다. 검증은 서비스 내부에서 수행한다 (`validateAccess`, `isAdmin`, `requirePositiveQuantity` 등).
  - 정규화/검증/권한 체크는 별도 private 메서드로 분리한다 (`normalize`, `validateDuplicatedName`, `findProductById`, `getUserOrThrow`).
  - 서비스 리턴 타입은 도메인 모델이 아닌 응답 DTO 를 그대로 반환한다 (`ResGetProductDtoV1.of(product)` 패턴). 이렇게 하면 컨트롤러는 전달만 담당한다.
  - 중복 호출 방지나 멱등성 보장은 서비스 계층에서 구현한다 (예: `productStockRepository.existsByProductIdAndOrderIdAndType(...)` 체크 후 저장).
- 예외:
  - 서비스 계층에서 발생시키는 예외는 `presentation.advice` 패키지의 `*Exception`/`*Error` 를 사용한다. 오류코드는 enum 으로 관리하며, 응답 메시지를 전역 처리기가 반환한다.
- 기타:
  - Redis·외부 시스템 연동은 별도 클라이언트를 주입하고, 서비스에서 직접 호출한다. 테스트에서는 `@MockitoBean` 으로 해당 클라이언트를 대체한다.
  - 문자열 입력은 `normalize` 메서드로 공백을 trim 후 비어 있으면 `null` 로 치환해 검색 조건이나 검증 실패를 명확히 한다.

### 테스트 컨벤션 (참고)
- `@WebMvcTest` 환경에서는 `@MockitoBean` 으로 서비스/외부 클라이언트를 주입하고, 필수 프로퍼티는 `@DynamicPropertySource` 또는 `@TestConfiguration` 빈으로 제공한다.
- MockMvc 응답 검증 시 JSON Path 는 `ApiDto` 구조(`$.data.*`, `$.message`) 를 기준으로 작성한다.
- JWT 설정은 `DynamicPropertyRegistry` 로 주입하거나, 간단한 경우 `properties` 속성에 기본값을 넣는다.

## 진행 상황 메모
- RestDocs 기반 컨트롤러 테스트 작성 완료: product / user / order / payment 서비스. product 내부 전용 API(`release-stock`, `return-stock`)까지 문서화 포함.
- `user` 서비스 액세스 토큰 검증 응답에서 `jwtValidatorTimestamp` 필드를 제거했고 테스트/문서 스니펫 갱신 필요.
- `user` 서비스 인증/회원 API 는 실제 UserRepository + Redis 기반으로 동작하며 JwtAuthorizationFilter 로 보호된다. Gateway 는 동일한 시크릿/Redis 정보를 활용해 AccessTokenValidationFilter 로 1차 검증을 수행하고, `/v1/auth/check-access-token` 은 보조 확인 용도로 유지된다.
- Gateway 서비스는 AccessTokenValidationFilter 로 JWT 서명/Redis deny 검사를 수행한다. 추가로 로깅·모니터링·서킷브레이커 도입 여부를 검토해야 한다.
- Resilience4j fallback 자동 구성은 의존성 버전을 낮춰 해결(설정 변경 무효화).
- RestDocs 스니펫 → OpenAPI merge 파이프라인 구성 완료.
- user 서비스 Spring Security 및 JPA Auditing 구성 완료.

## 후속 작업 아이디어
- product/internal API에 대한 실제 재고 처리 로직 및 멱등성 구현(현재는 더미 응답).
- Order/Payment 서비스가 product 내부 API 호출 시 사용할 클라이언트(fallback 전략 포함) 설계 반영.
- Gateway 공통 로깅/관찰성 및 서킷 브레이커/Rate Limiter 적용.