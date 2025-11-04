# 서비스 인터페이스 및 상호 연동 정리

본 문서는 `example-shop-springboot-msa-mark1` 전환 작업을 위한 Product · Order · User · Gateway 서비스의 REST 계약과 내부 연동 규칙을 정의한다. 모든 응답은 공통 모듈의 `ApiDto<T>` 포맷을 따른다.

## 공통 규칙
- 내부 API(`*/internal/v1/*`) 호출 시에도 `Authorization: Bearer <accessJwt>` 헤더를 필수로 전달한다. Order·Payment 서비스는 `CustomUserDetails.accessJwt` 값을 그대로 재사용한다.
- Resilience4j CircuitBreaker/Retry 설정은 Config Server 프로퍼티로 주입되며, RestTemplate 호출 타임아웃은 4초(연결/읽기)로 통일했다.
- 성공 응답은 200, 비즈니스 검증 실패는 400/403/404 를 사용하며, 상세 원인은 `ApiDto.code` 와 `message` 로 판별한다.

## Product 서비스 (`com.example.shop.product`)

### 퍼블릭 API (`/v1/products`)

| 메서드 | 엔드포인트 | 설명 | 쿼리/바디 | 정상 응답 | 주요 에러 코드(HTTP 4xx) |
| --- | --- | --- | --- | --- | --- |
| GET | `/v1/products` | 상품 목록 조회 | `page`, `size`, `sort`, `name`(선택, 부분 일치) | 200 + `productPage`(id, name, price, stock) | `PRODUCT_BAD_REQUEST` |
| GET | `/v1/products/{id}` | 단일 상품 조회 | - | 200 + `product` 객체 | `PRODUCT_CAN_NOT_FOUND` |
| POST | `/v1/products` | 상품 등록 | `{"product": {"name": "...", "price": 0+, "stock": 0+}}` | 200 + 신규 상품 id | `PRODUCT_NAME_DUPLICATED`, `PRODUCT_BAD_REQUEST` |
| PUT | `/v1/products/{id}` | 상품 수정 | `{"product": {"name":?, "price":?, "stock":?}}` | 200 + 수정된 상품 id | `PRODUCT_CAN_NOT_FOUND`, `PRODUCT_NAME_DUPLICATED`, `PRODUCT_FORBIDDEN` |
| DELETE | `/v1/products/{id}` | 상품 삭제(soft delete) | - | 200 | `PRODUCT_CAN_NOT_FOUND`, `PRODUCT_FORBIDDEN` |

> 권한: `POST/PUT/DELETE` 는 ADMIN/MANAGER 역할만 가능.

### 내부 API (`/internal/v1/products`)

| 메서드 | 엔드포인트 | 설명 | 요청 바디 | 정상 응답 | 실패 시 응답 |
| --- | --- | --- | --- | --- | --- |
| POST | `/internal/v1/products/release-stock` | 주문 생성 시 다수 상품 재고 차감 | `{"order":{"orderId"},"productStocks":[{"productId","quantity"}...]}` | 200 + `ApiDto`(`message = "상품 재고 차감이 완료되었습니다."`) | 400 + `ApiDto`(`code` 값으로 원인 식별) |
| POST | `/internal/v1/products/return-stock` | 주문 취소/보상 시 재고 복원 | `{"order":{"orderId"}}` | 200 + `ApiDto`(`message = "상품 재고 복원이 완료되었습니다."`) | 400 + `ApiDto`(`code` 값으로 원인 식별) |

#### 내부 API 에러 코드
- `PRODUCT_BAD_REQUEST`: 필수 필드 누락, 재고 부족, 동일 주문에 대한 중복 RELEASE/RETURN 요청 등 모든 검증 실패를 현재 이 코드로 통일한다.
- `PRODUCT_CAN_NOT_FOUND`: 컨트롤러/서비스 공통 검증에서 상품이 존재하지 않을 때 사용한다(단, `release-stock` bulk 처리에서는 존재하지 않는 상품이 요청에 포함되면 현재 아무 작업도 수행하지 않으므로 추후 보완 필요).

> HTTP 상태는 200(성공) / 400(실패)로 통일하고, 호출 측은 `ApiDto.code` 와 `message` 로 장애 원인을 판별한다.

### 도메인/데이터 구현 현황
- 재고 변동 이력은 `ProductStock`(도메인) ↔ `ProductStockEntity`(JPA) 로 관리한다. 엔티티에는 `(product_id, order_id, type)` 유니크 제약이 걸려 있어 동일 주문이 같은 동작을 두 번 수행하지 못한다.
- `ProductStockRepository` 는 다음 보조 메서드를 제공한다.  
  - `existsByOrderIdAndType(orderId, type)`: 주문 단위 멱등성 검사(RELEASE/RETURN 각각).  
  - `findByOrderId(orderId)`: RETURN 시 기존 RELEASE 레코드를 일괄 조회.  
  - `findByIdIn(productStockIdList)`: 추후 보상/감사 기능에서 ledger id 기반 조회에 사용.  
  - `existsByProductIdAndOrderIdAndType(...)`: 특정 상품 단위로 중복 체크가 필요할 때 사용.
- `ProductServiceV1.postInternalProductsReleaseStock` 흐름:  
  1. 주문 ID 로 RELEASE 기록이 이미 있는지 검사(`existsByOrderIdAndType`).  
  2. 요청에 포함된 상품 ID 목록을 `productRepository.findByIdIn` 으로 조회해 현재 재고를 확인하고, 차감 가능한 수량인지 검사한다.  
  3. 재고를 차감한 뒤, 각 품목에 대해 `ProductStock` ledger(RELEASE 타입)를 저장한다.
- `ProductServiceV1.postInternalProductsReturnStock` 흐름:  
  1. RETURN 기록 중복 여부를 선검사한다(`existsByOrderIdAndType(orderId, RETURN)`).  
  2. 기존 RELEASE ledger 를 `findByOrderId` 로 불러와 연결된 상품을 복원하고, 동일 수량만큼 재고를 증가시킨다.  
  3. 복원 완료 후 RETURN 타입 ledger 를 추가로 저장한다.
- 두 내부 메서드 모두 하나의 트랜잭션에서 재고 변경과 ledger 기록을 수행해, 중간 실패 시 DB 가 롤백된다.
- 현재 구현은 낙관적 락/비관적 락을 사용하지 않는다. 고동시성 환경에서는 `product.stock` 컬럼에 버전 또는 DB 락 전략을 추가하는 방안을 별도로 검토해야 한다.

#### API ↔ 도메인 매핑
- `release-stock` 성공 → Product 재고 감소 + `ProductStock(type = RELEASE)` 기록 1건씩 생성.
- `return-stock` 성공 → Product 재고 증가 + `ProductStock(type = RETURN)` 기록이 추가로 남는다.
- `release-stock` 실패(중복 요청·재고 부족 등) → 변경 사항 없이 400 + `PRODUCT_BAD_REQUEST`.
- `return-stock` 실패(중복 RETURN 등) → 변경 사항 없이 400 + `PRODUCT_BAD_REQUEST`.

## Order 서비스 (`com.example.shop.order`)

### 퍼블릭 API (`/v1/orders`)

| 메서드 | 엔드포인트 | 설명 | 쿼리/바디 | 정상 응답 | 주요 에러 코드(HTTP 4xx) |
| --- | --- | --- | --- | --- | --- |
| GET | `/v1/orders` | 주문 목록 조회 | `page`, `size`, `sort` | 200 + `orderPage`(id, status, totalAmount, createdAt) | `ORDER_FORBIDDEN` |
| GET | `/v1/orders/{id}` | 주문 상세 조회 | - | 200 + 주문 + 주문상품 + 결제 요약 | `ORDER_NOT_FOUND`, `ORDER_FORBIDDEN` |
| POST | `/v1/orders` | 주문 생성 | `{"order": {"orderItemList": [{"productId": "...", "quantity": 1+}, ...]}}` | 200 + `order`(id) | `ORDER_BAD_REQUEST`, `ORDER_PRODUCT_NOT_FOUND`, `ORDER_PRODUCT_OUT_OF_STOCK` |
| POST | `/v1/orders/{id}/cancel` | 주문 취소 | - | 200 + 메시지(`{orderId} 주문이 취소되었습니다.`) | `ORDER_NOT_FOUND`, `ORDER_ALREADY_CANCELLED`, `ORDER_FORBIDDEN` |

> 권한: 일반 사용자는 본인 주문만 조회/취소 가능, ADMIN/MANAGER 는 전체 조회/취소 가능.

### Product 서비스 호출 규칙
1. `POST /v1/orders` 처리 시 주문 내 모든 상품을 한 번의 요청으로 `POST /internal/v1/products/release-stock` 에 전달한다.
2. 호출이 실패하면, 이미 차감된 품목 목록으로 `POST /internal/v1/products/return-stock` 을 호출하고 주문을 롤백한다.
3. 주문 취소(`POST /v1/orders/{id}/cancel`) 시 동일 DTO 구조를 사용해 `return-stock` 호출 후 주문 상태를 `CANCELLED` 로 갱신한다.
4. `orderId` 는 주문 서비스가 생성한 UUID 를 사용하고, Product 측 Ledger/Reservation 과 매핑하여 멱등성·중복 방지를 구현한다.

### 에러 처리
- Product 호출 실패 시 Order 서비스는 Product 의 `ApiDto.code` 를 해석해 사용자/시스템 메시지를 결정한다.  
  예) `PRODUCT_STOCK_NOT_ENOUGH` → 주문 생성 실패, 주문 레코드 저장 금지.
- Order 서비스 자체 오류는 기존 `OrderError` 코드를 유지(HTTP 400/403/404).
- 보상 호출이 실패할 경우를 대비해 Retry/CircuitBreaker 전략을 Resilience4j 로 명시한다. 보상 실패가 반복될 경우 DB 트랜잭션에 보상 상태를 기록하고 운영 경고를 발생시키는 절차가 필요하다.

### Payment 서비스 호출 규칙
1. 주문 취소 시 결제 정보가 존재하고 `status = COMPLETED` 인 경우 `POST /internal/v1/payments/{paymentId}/cancel` 을 호출해 결제를 먼저 취소한다.
2. Payment 내부 API 는 바디 없이 호출하며, 성공 시 `ApiDto.message = "{paymentId} 결제가 취소되었습니다."` 를 반환한다.
3. 실패 에러 코드 처리  
   - `PAYMENT_NOT_FOUND`, `PAYMENT_ALREADY_CANCELLED`: `PaymentRestTemplateClientV1` 이 `OrderException(OrderError.ORDER_BAD_REQUEST)` 로 변환한다(현재 구현은 취소를 중단하며, 향후 무시 정책으로 전환 가능).  
   - 기타 오류: 동일하게 `ORDER_BAD_REQUEST` 로 변환한다.
4. Payment 취소가 성공했을 때만 Product `return-stock` 을 호출한다. 결제 취소 단계에서 예외가 발생하면 주문 상태/재고는 변경되지 않는다.

## Payment 서비스 (`com.example.shop.payment`)

### 퍼블릭 API (`/v1/payments`)

| 메서드 | 엔드포인트 | 설명 | 요청/파라미터 | 정상 응답 | 주요 에러 코드 |
| --- | --- | --- | --- | --- | --- |
| GET | `/v1/payments/{id}` | 결제 단건 조회 | Path `id` (UUID) | 200 + `payment`(id,status,method,amount,approvedAt,orderId,transactionKey) | `PAYMENT_NOT_FOUND`, `PAYMENT_FORBIDDEN` |
| POST | `/v1/payments` | 결제 생성 | `{"payment":{"orderId","method","amount"}}` | 200 + `payment`(id,status,method,amount,approvedAt,orderId,transactionKey) | `PAYMENT_BAD_REQUEST`, `PAYMENT_INVALID_AMOUNT` |

> 권한: 본인 결제만 조회/생성 가능. ADMIN/MANAGER 정책은 추후 확장 가능.

### 내부 API (`/internal/v1/payments`)

| 메서드 | 엔드포인트 | 설명 | 요청 바디 | 정상 응답 | 실패 시 응답 |
| --- | --- | --- | --- | --- | --- |
| POST | `/internal/v1/payments/{id}/cancel` | 주문 취소 시 결제 보상 | - (헤더만 전송) | 200 + `ApiDto`(`message = "{paymentId} 결제가 취소되었습니다."`) | 400 + `ApiDto`(`code = PAYMENT_NOT_FOUND / PAYMENT_ALREADY_CANCELLED / PAYMENT_BAD_REQUEST`) |

### 도메인/연동 메모
- `PaymentServiceV1.postPayments` 는 결제를 `COMPLETED` 상태로 저장한 뒤 Order 서비스 `POST /internal/v1/orders/{id}/complete` 를 호출해 주문 상태를 동기화한다.
- `PaymentServiceV1.postInternalPaymentsCancel` 은 결제를 `CANCELLED` 로 마킹하고 저장한다. 이미 취소된 결제는 `PAYMENT_ALREADY_CANCELLED` 오류를 발생시킨다.
- RestTemplate 호출 전용 클라이언트  
  - `OrderRestTemplateClientV1`: 결제 성공 시 주문 완료 알림.  
  - (신규) `InternalPaymentControllerV1` + Order 서비스 `PaymentRestTemplateClientV1`: 주문 취소 시 결제 취소를 수행.
- 결제 도메인은 아직 외부 PG 연동이 없으며, `transactionKey` 생성/저장은 TODO 상태다.

## Gateway 서비스 (`com.example.shop.gateway`)

### 현재 구성
- Config Server 의 `gateway-service-dev.yml` 기준 포트는 `19200` 이며, Spring Cloud Gateway(WebFlux) + Eureka + Config 조합으로 기동한다.
- `spring.cloud.gateway.server.webflux.discovery.locator.enabled=true` 로 서비스 디스커버리를 켜 두었지만, 동시에 `server.webflux.routes` 에 user/product/order/payment 수동 라우팅을 선언해 경로/문서 파일을 명시적으로 관리한다.
- 각 라우팅은 `lb://{service-name}` 으로 전달되며 `spring-cloud-starter-loadbalancer` 만 사용한다(별도 Netty 필터 없음).
- `AccessTokenValidationFilter` 가 전역 필터로 동작하면서 Config Server 로부터 전달받은 `shop.security.jwt.*` 값을 사용해 JWT 서명을 검증하고, 토큰의 `id` 클레임과 Redis 에 저장된 `auth:deny:{userId}` 값을 비교해 만료/무효 여부를 판단한다. 필터를 통과한 요청은 추가 가공 없이 원본 헤더를 유지한 채 백엔드 서비스로 전달된다.

### 인증/인가 흐름
1. user 서비스에서 `accessJwt`/`refreshJwt` 를 발급하면 JWT 서명(secret, subject 등) 과 토큰 만료 시간 설정 값은 Config Server 를 통해 user·gateway 양쪽 모두에 공유된다.
2. Gateway 로 유입된 모든 외부 요청은 `AccessTokenValidationFilter` 를 거치며, `Authorization: Bearer <token>` 헤더가 없는 경우(공개 API)만 통과한다.
3. 필터는 HMAC512 서명을 검증한 뒤, 토큰에서 추출한 사용자 ID 로 Redis(`auth:deny:{userId}`) 의 deny 타임스탬프를 조회한다.  
   - deny 값이 존재하고 토큰 `iat`(또는 `jwtValidator`) 보다 크거나 같으면 401 로 차단.  
   - deny 값이 없거나 과거 값이면 인증 통과.
4. 차단되지 않은 요청은 토큰을 재발급하거나 재검증하지 않고 그대로 다운스트림 서비스에 전달한다. 이후 개별 서비스는 JWT 클레임(역할, 사용자 ID 등)을 사용해 인가 판단을 수행한다.
5. 모든 외부 진입점이 Gateway 이므로 user 서비스 데이터베이스 조회 없이 Redis 만을 사용하는 경량 인증 경로를 확보한다.

### 라우팅 테이블 (dev)

| Route ID | Path 패턴 | 대상 URI | 비고 |
| --- | --- | --- | --- |
| `user-service` | `/v*/users/**`, `/v*/auth/**`, `/springdoc/openapi3-user-service.json` | `lb://user-service` | 사용자 API + user 서비스가 노출하는 OpenAPI 정적 파일 |
| `product-service` | `/v*/products/**`, `/springdoc/openapi3-product-service.json` | `lb://product-service` | 상품 REST + 문서 |
| `order-service` | `/v*/orders/**`, `/springdoc/openapi3-order-service.json` | `lb://order-service` | 주문 REST + 문서 |
| `payment-service` | `/v*/payments/**`, `/springdoc/openapi3-payment-service.json` | `lb://payment-service` | 결제 REST + 문서 |

`Path=/v*/...` 패턴을 사용하므로 `v1`, `v2` 등 향후 버전 경로도 동일한 라우팅 규칙에 포함된다.

### Swagger/OpenAPI 노출
- 각 서비스 Gradle 스크립트의 `setDocs` 작업이 빌드 시 `build/api-spec/*.json(yaml)` 을 `static/springdoc/openapi3-<service>.{json|yaml}` 로 복사한다.
- Gateway 는 `springdoc.swagger-ui.urls` 로 네 서비스를 등록하고 `/docs` 경로에서 UI 를 제공한다. 정적 스펙 파일은 Gateway 로 유입되지만 실제 JSON 은 각 서비스 정적 리소스를 proxy 하는 구조다.

### 관측된 공백/추가 TODO
- Resilience4j 의존성만 등록되어 있고 Gateway 레벨 CircuitBreaker/RateLimit/Retry 설정이 비어 있다. Config Server 상의 `resilience4j` 기본 구성은 선언되어 있으나 실제 라우트에 바인딩되어 있지 않다.
- 감사 로깅, Zipkin trace logging, Rate Limiter, CORS 정책 등 Gateway 차원의 공통 기능이 미정이라 향후 결정이 필요하다.
- deny 키 TTL(현재 30분) 및 Redis 장애 대비 정책을 운영 관점에서 확정해야 한다.

## 공통 정책 요약
- 모든 서비스는 `/v1` 로 시작하는 버전명을 사용한다. 내부 전용 엔드포인트는 `/internal/v1` 네임스페이스를 사용해 Gateway 외부 노출을 차단한다.
- HTTP 200/400 중심으로 상태 코드를 단순화하고, 상세 오류는 `ApiDto.code` 와 `message` 로 전달한다.
- Zipkin trace id 는 `X-B3-TraceId` 등 Spring Cloud Sleuth 기본 헤더로 전파한다.
- RestTemplate 호출에는 Resilience4j CircuitBreaker + Retry 를 적용하고, 타임아웃 및 fallback 정책을 `application.yml` 에 정의한다.

## 향후 확인 필요 사항
1. Payment 취소 오류(`PAYMENT_NOT_FOUND`, `PAYMENT_ALREADY_CANCELLED`) 를 Order 취소 흐름에서 허용할지 정책 확정 및 예외 매핑 조정.
2. 재고 차감/보상·결제 취소 등 멀티 서비스 통합 시나리오 테스트를 WireMock/Testcontainers 기반으로 자동화.
3. Gateway 레벨 CircuitBreaker/Rate Limiter/모니터링 정책 확정: JWT 검증 필터만 존재하므로 관찰성·탄력성 구성이 필요하다.
4. User 서비스 DB 마이그레이션 및 Redis 장애 대비 전략 정립: dev 환경은 H2/인메모리 Redis 가정이므로 실제 운영 DB/Failover 전략이 필요하다.

## User 서비스 (`com.example.shop.user`)

### 현재 구성
- Config Server 의 `user-service-dev.yml` 에 따라 dev 포트는 `19300` 이며 H2(in-memory) + JPA + QueryDSL 을 사용한다. `ddl-auto=update` 로 테이블을 생성하고, JPA Auditing 이 켜져 있다.
- `AuthServiceV1` 과 `UserServiceV1` 로 인증/회원 도메인을 분리했고, `UserRepositoryImpl` 이 QueryDSL 기반 검색/페이지네이션을 담당한다.
- Redis(StringRedisTemplate) 는 `auth:deny:{userId}` 키로 토큰 무효화 시점을 저장한다. JWT 치환 로직은 `JwtTokenGenerator` + `JwtAuthorizationFilter` 로 구성된다.
- `RestTemplateConfig` 가 `@LoadBalanced` `RestTemplate` 를 노출하므로 추후 다른 MSA 호출 시 공통 구성을 재활용할 수 있다.

### 인증 API (`/v1/auth`)

| 메서드 | 엔드포인트 | 설명 | 요청/파라미터 | 응답/비고 |
| --- | --- | --- | --- | --- |
| POST | `/v1/auth/register` | 신규 사용자 등록 | `{"user":{"username","password","nickname","email"}}` | `ApiDto` 메시지, 기본 ROLE.USER 부여 및 비밀번호 BCrypt 저장 |
| POST | `/v1/auth/login` | 로그인 및 토큰 발급 | `{"user":{"username","password"}}` | `ApiDto.data` 에 `accessJwt`, `refreshJwt` |
| POST | `/v1/auth/refresh` | 리프레시 토큰 검증/재발급 | `{"refreshJwt":"..."}` | 새 access/refresh JWT. 사용자 `jwtValidator` 값이 최신 토큰보다 크면 400 |
| POST | `/v1/auth/check-access-token` | 액세스 토큰 검증 | `{"accessJwt":"..."}` | `ApiDto.data = {userId, valid, remainingSeconds}`. Redis `auth:deny:*` 와 DB 의 `jwtValidator` 값을 모두 확인 |
| POST | `/v1/auth/invalidate-before-token` | 기 발급 토큰 무효화(모든 기기 로그아웃) | 인증 필요. Body `{"user":{"id":"<uuid>"}}` | 대상 사용자의 `jwtValidator` 를 현재 epoch-second 로 갱신하고 Redis 블랙리스트에 기록 |

> `/v1/auth/invalidate-before-token` 은 본인/ADMIN/MANAGER 만 호출 가능하며, ADMIN 계정에 대해서는 ADMIN 만 갱신할 수 있다.

### 사용자 관리 API (`/v1/users`)

| 메서드 | 엔드포인트 | 설명 | 인증/권한 | 요청 | 응답/주요 에러 |
| --- | --- | --- | --- | --- | --- |
| GET | `/v1/users` | 사용자 목록 조회 (검색/페이지) | ADMIN, MANAGER | `page`,`size`,`sort`,`username`,`nickname`,`email` (선택) | `ApiDto.data.userPage` (`PagedModel`). 미인가 시 `USER_FORBIDDEN` |
| GET | `/v1/users/{id}` | 단일 사용자 조회 | 본인 또는 ADMIN/MANAGER | Path `id` (UUID) | `ApiDto.data.user`. 타 사용자를 조회할 권한 없을 경우 `USER_BAD_REQUEST`, 존재하지 않으면 `USER_CAN_NOT_FOUND` |
| DELETE | `/v1/users/{id}` | 사용자 삭제(soft delete) | 본인 또는 ADMIN/MANAGER (단, ADMIN 대상 삭제 금지) | Path `id` (UUID) | `ApiDto` 메시지. 삭제 시 Redis 블랙리스트 등록. 권한 위반/대상 ADMIN 삭제 시 `USER_BAD_REQUEST` |

### 인증/토큰 관리 메모
- JWT 설정(`shop.security.jwt.*`) 은 Config Server 로부터 주입되며 access 30분, refresh 180일이 기본값이다. user 서비스의 `JwtAuthorizationFilter` 와 gateway 의 `AccessTokenValidationFilter` 가 동일한 시크릿과 만료 정책을 사용한다.
- `JwtAuthorizationFilter` 는 Gateway 를 통과해 들어온 요청에 대해서도 다시 한 번 서명을 검증하고, 토큰의 `id` 클레임과 JPA 로 관리되는 `jwtValidator` 값을 비교해 무효화 여부를 판단한다. Gateway 를 우회한 내부 호출에 대해서도 동일한 보안 수단을 제공한다.
- `AuthRedisClient` 는 `denyBy(userId, jwtValidator)` 로 Redis 키를 저장하며 TTL 은 30분(Access Token 기본 만료 시간)으로 고정한다. Redis 에 deny 값이 존재하면 Gateway 및 user 서비스 모두에서 즉시 차단된다.
- `invalidateBeforeToken`, 사용자 정보 변경, 삭제 등의 이벤트가 발생하면 도메인 계층에서 `jwtValidator` 를 현재 epoch-second 로 갱신하고 Redis 에도 새 값을 기록해 모든 기존 토큰을 즉시 무효화한다.
- `SecurityConfig` 는 `/v1/auth/**`, `/docs/**`, `/springdoc/**`, `/actuator/health|info` 만 익명 허용하며 나머지는 JWT 인증 필터를 거친다. dev 프로필일 때만 `/h2/**` 를 오픈한다.

### 서비스 연동 포인트
- Order 등 후속 서비스는 Gateway 를 통해 들어온 요청의 JWT 클레임(사용자 ID, 역할)을 신뢰하고, 자체 인가 정책만 수행한다. 토큰 재검증은 Gateway 와 user 서비스에서 이미 처리되므로 중복 호출이 필요 없다.
- `/v1/auth/check-access-token` 엔드포인트는 외부 시스템(예: 백오피스) 또는 테스트 시나리오에서 토큰 상태를 확인하기 위한 용도로 유지된다. 서비스 간 통합 흐름에서는 Redis 조회 기반 필터가 기본 경로다.
- `AuthServiceV1` 이 JWT 발급/무효화의 단일 진입점이므로 다른 서비스에서 직접 JWT 를 만들지 않는다.

## 진행 메모 (미완료/추가 확인)
- user 인증 API `/v1/auth/check-access-token` 은 `userId`, `valid`, `remainingSeconds` 만 반환하며 보조 검증용으로 유지 중이다. 추가 메타 정보(역할 등)가 필요하면 DTO 확장 여부를 먼저 결정할 것.
- Product 내부 API(`release-stock`, `return-stock`) 는 주문 기반 ledger와 재고 갱신까지 구현되어 있다. Order/Payment 통합 테스트 및 예외 시뮬레이션은 추후 보완해야 한다.
- Gateway 에는 공통 로깅, 모니터링, 서킷 브레이커, Rate Limiter 등 보강 과제가 남아 있다.
- RestDocs → OpenAPI 산출물 경로: 각 서비스 `build/api-spec/` 확인. 중앙 문서화/배포 정책은 추후 결정.
