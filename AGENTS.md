# example-shop-springboot-msa-mark1 가이드

## 현재 상태 요약
- 7개 마이크로서비스(`config`, `eureka`, `gateway`, `user`, `product`, `order`, `payment`)가 Spring Boot 3 + Spring Cloud 기반으로 분리되어 있으며, 공통 코드는 `com.github.jaybon1:example-shop-springboot-msa-global:0.0.1` 아티팩트를 통해 공유한다.
- 모든 REST 응답은 `ApiDto<T>` 래퍼를 사용하고, 외부 공개 API는 `/v1`, 서비스 간 내부 API 는 `/internal/v1` 네임스페이스를 따른다.
- 서비스 간 호출은 `@LoadBalanced RestTemplate` 를 사용하며, JWT(`Authorization: Bearer <accessJwt>`) 를 그대로 전달해 다운스트림 서비스의 인가 검사에 활용한다.
- 탄력성은 Resilience4j(CircuitBreaker + Retry) 설정으로 구성되어 있고, Zipkin(Tracing), Eureka(서비스 디스커버리), Config Server(외부 설정)와 연동한다.

## 공통 인프라 & 실행
- 로컬 기동 순서: `config` → `eureka` → `zipkin` → `gateway` → (user/product/order/payment). 각 서비스는 `application.yml` 에서 Config/Eureka/Zipkin 주소를 참조한다.
- Gateway 는 모든 외부 진입점이며 `AccessTokenValidationFilter` 로 JWT 서명을 검증하고 Redis deny 리스트를 확인한 다음, 인증 헤더를 유지한 채 백엔드로 전달한다.
- 테스트 실행:  
  - Order 서비스 `GRADLE_USER_HOME=/tmp/gradle-order ./gradlew test` (디렉터리 `com.example.shop.order`)  
  - Payment 서비스 `GRADLE_USER_HOME=/tmp/gradle-payment ./gradlew test` (디렉터리 `com.example.shop.payment`)
- API 문서는 각 서비스 빌드 시 `build/api-spec` 에 생성되며 Gateway 의 `/docs` 경로에서 통합 Swagger UI 로 확인할 수 있다.

## 서비스별 주요 내용

### user 서비스
- JWT 발급/무효화 및 사용자 CRUD 를 담당하며 H2 기반으로 dev 환경을 구성했다.
- `JwtAuthorizationFilter` 가 모든 요청을 재검증하고 `CustomUserDetails` 에 사용자 정보 + `accessJwt` 를 담아 컨트롤러에 전달한다.

### product 서비스
- 상품 CRUD + 재고 관리, 내부 전용 API(`release-stock`, `return-stock`) 를 통해 주문 서비스에 재고 차감/보상을 제공한다.
- Ledger(`ProductStock`) 로 멱등성을 보장하고, 주문 ID 기준 중복 요청을 차단한다.

### order 서비스
- 주문 생성 시 `ProductRestTemplateClientV1` 으로 재고를 한 번에 차감하고, 주문 취소/보상 시 재고 복원 API 를 호출한다.
- 결제 완료 시점에는 Payment 서비스 내부 API 를 통해 주문 상태를 갱신하며, 주문 취소 시 이미 완료된 결제가 있다면 `PaymentRestTemplateClientV1.postInternalPaymentsCancel` 로 결제 취소를 선처리한다.
- 모든 RestTemplate 호출은 `CustomUserDetails.accessJwt` 를 Bearer 토큰으로 전달한다.

### payment 서비스
- 사용자 결제(현재는 즉시 `COMPLETED` 처리)와 주문 상태 동기화를 담당한다.
- 주문 완료 통보: `OrderRestTemplateClientV1.postInternalOrdersComplete`.  
  주문 취소 보상: `InternalPaymentControllerV1`(`POST /internal/v1/payments/{paymentId}/cancel`) → `PaymentServiceV1.postInternalPaymentsCancel`.
- `PAYMENT_ALREADY_CANCELLED` 오류 코드를 통해 중복 취소 요청을 구분한다.

### gateway 서비스
- `/v*/{resource}` 패턴으로 user/product/order/payment 를 라우팅하고, 각 서비스의 OpenAPI 정적 파일을 프록시한다.
- JWT 검증 실패 시 401 응답, Redis deny 리스트에 등록된 사용자도 즉시 차단한다.

## 상호 연동 흐름
1. **주문 생성**  
   - Order 서비스가 상품 목록을 검증 → Product 서비스 `POST /internal/v1/products/release-stock`.  
   - 성공 시 주문 저장, 실패 시 주문 트랜잭션 롤백.
2. **결제 완료**  
   - Payment 서비스가 자체 트랜잭션 저장 후 Order 서비스 `POST /internal/v1/orders/{id}/complete` 호출.  
   - 주문이 이미 취소/결제 상태면 `ORDER_ALREADY_CANCELLED/PAID` 반환.
3. **주문 취소**  
   - Order 서비스가 결제 정보 확인 → 결제가 `COMPLETED` 면 Payment 서비스 `POST /internal/v1/payments/{paymentId}/cancel`.  
   - 이후 Product 서비스 `POST /internal/v1/products/return-stock` 으로 재고 복원, 주문 상태를 `CANCELLED` 로 저장.

모든 상호 호출은 Authorization 헤더를 전달해야 하며, 실패 시 `ApiDto.code` 를 기준으로 재시도/보상 로직을 분기한다.

## 잔여 과제 / 향후 개선
- Gateway 관측성 향상: 서킷 브레이커, Rate Limiter, 공통 로깅/메트릭 설정.
- Product/Order/Payment 통합 테스트(WireMock/Testcontainers) 작성 및 장애 시나리오 자동화.
- 운영 DB/Redis 환경 도입 시 실패 복구 전략(멀티 AZ, TTL 정책 등) 확정.
