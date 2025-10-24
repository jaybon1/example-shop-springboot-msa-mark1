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
3. `presentation/controller` 및 DTO 를 참고하여 `/api/v1/products` REST 엔드포인트와 내부 전용 `/internal/v1/products/{productId}/stock-release`, `/internal/v1/products/{productId}/stock-return` API 를 정의하고, Gateway 라우팅 정보와 일치하도록 스키마를 확정한다.
4. JPA 설정, CommandLineRunner 기반 초기 데이터 로딩(`ProductCommandLineRunner`) 을 profile 기반으로 분리하여 개발과 운영 환경 차이를 최소화한다.
5. 통합 테스트 시나리오(상품 CRUD + 재고 확인)를 `@SpringBootTest` 또는 `@DataJpaTest` 로 구성하고, Zipkin trace id 가 정상 전파되는지 확인한다.

### 3. order 서비스 전환 (`com.example.shop.order`)
1. 모놀리식 `Order`, `OrderItem` 도메인 모델과 `OrderEntity`, `OrderItemEntity`, `OrderMapper` 구조를 그대로 재현하되, 주문 생성 시 user/product 서비스에 대한 동기 호출 흐름을 명시한다.
2. `OrderServiceV1` 의 주문 생성/조회/취소 비즈니스 로직을 마이크로서비스 구조로 이관하면서 다음과 같은 외부 연동 포인트를 정의한다:  
   - 사용자 유효성 검증: user 서비스 `GET /api/v1/users/{id}`  
   - 상품 재고 차감: product 서비스 `POST /internal/v1/products/{productId}/stock-release`  
   각 호출에 대해 `RestTemplate` + Resilience4j(CircuitBreaker + Retry) 설정을 `application.yml` 에 명시한다.
3. 여러 상품에 대한 재고 차감 중 하나라도 실패하면 전체 주문을 롤백하고, 이미 차감된 재고는 `POST /internal/v1/products/{productId}/stock-return` 보상 호출로 복원하도록 트랜잭션/보상 로직을 설계한다. CircuitBreaker open 상태에서 임시 주문 생성 차단 정책도 포함한다.
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

## 진행 상황 메모
- RestDocs 기반 컨트롤러 테스트 작성 완료: product / user / order / payment 서비스. product 내부 전용 API(`stock-release`, `stock-return`)까지 문서화 포함.
- `user` 서비스 액세스 토큰 검증 응답에서 `jwtValidatorTimestamp` 필드를 제거했고 테스트/문서 스니펫 갱신 필요.
- `user` 서비스 인증 엔드포인트(`register`, `login`, `refresh`, `access-token-check`) 더미 구현 및 RestDocs 연동 완료.
- Resilience4j fallback 자동 구성은 의존성 버전을 낮춰 해결(설정 변경 무효화).
- RestDocs 스니펫 → OpenAPI merge 파이프라인 구성 완료.

## 후속 작업 아이디어
- product/internal API에 대한 실제 재고 처리 로직 및 멱등성 구현(현재는 더미 응답).
- Order/Payment 서비스가 product 내부 API 호출 시 사용할 클라이언트(fallback 전략 포함) 설계 반영.
