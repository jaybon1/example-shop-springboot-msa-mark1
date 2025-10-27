# 서비스 인터페이스 및 상호 연동 정리

본 문서는 `example-shop-springboot-msa-mark1` 전환 작업을 위한 Product · Order · User · Gateway 서비스의 REST 계약과 내부 연동 규칙을 정의한다. 모든 응답은 공통 모듈의 `ApiDto<T>` 포맷을 따른다.

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
| POST | `/internal/v1/products/{productId}/stock-release` | 주문 생성 시 재고 차감 및 확정 | `{"orderId": "...", "quantity": 1+}` | 200 + `ApiDto`(`code = PRODUCT_STOCK_RELEASED`, `data`에 현재 재고) | 400 + `ApiDto`(`code` 값으로 원인 식별) |
| POST | `/internal/v1/products/{productId}/stock-return` | 주문 취소 또는 보상 시 재고 복원 | `{"orderId": "...", "quantity": 1+}` | 200 + `ApiDto`(`code = PRODUCT_STOCK_RETURNED`) | 400 + `ApiDto`(`code` 값으로 원인 식별) |

#### 내부 API 에러 코드 제안
- `PRODUCT_STOCK_NOT_FOUND`: productId 존재하지 않음.
- `PRODUCT_STOCK_NOT_ENOUGH`: 요청 수량만큼 차감 불가.
- `PRODUCT_STOCK_ALREADY_RELEASED`: 동일 `orderId` 에 대한 중복 차감 요청.
- `PRODUCT_STOCK_NOT_RESERVED`: 반환 요청 시 기존 차감 기록 없음.
- `PRODUCT_STOCK_PAYLOAD_INVALID`: 주문 번호/수량 등 필드 오류.

> HTTP 상태는 200(성공) / 400(실패)로 통일하고, 호출 측은 `ApiDto.code` 로 장애 원인을 판별한다.

### 도메인/데이터 고려사항
재고 차감/복원 시 `orderId` 기반 멱등성 확보가 필요하다. 모놀리식 Product 도메인에는 차감 이력을 추적하는 모델이 없으므로, 마이크로서비스 전환 시 다음 중 하나의 신규 모델을 도입해야 한다.

- `ProductStockLedger` (권장):  
  - 컬럼: `id`, `productId`, `orderId`, `adjustmentType(RELEASE|RETURN)`, `quantity`, `status(COMPLETED|CANCELLED)`, `createdAt` 등  
  - 제약: `(productId, orderId, adjustmentType)` 유니크 인덱스 → 중복 요청 차단  
  - 반환 시 RELEASE 레코드를 조회하여 수량/상태 검증 후 RETURN 기록 추가
- 대안으로는 `ProductReservation` 엔티티를 두고 주문별 예약 상태를 관리하되, RELEASE/RETURN 전환 로직을 명확히 관리해야 한다.

동시성: 재고 테이블 업데이트 시 낙관적 락(version) 또는 `for update` 락을 적용해야 재고 오버-차감을 막을 수 있다. Ledger 와 함께 사용할 경우, 재고 업데이트와 Ledger 기록을 하나의 트랜잭션으로 묶는다.

#### Product 재고 모델 세부안
| 엔티티 | 주요 컬럼 | 제약 조건 / 용도 |
| --- | --- | --- |
| `product` | `id`, `name`, `price`, `stock`, `version`, ... | 기존 엔티티. `stock` 은 현재 가용 수량, `version`(낙관적 락) 필드를 추가해 동시 업데이트 충돌 방지 |
| `product_stock_ledger` | `id`, `product_id`, `order_id`, `adjustment_type`, `quantity`, `status`, `occurred_at`, `expired_at`, `metadata` | `(product_id, order_id, adjustment_type)` 유니크, `status` 는 `COMPLETED`, `COMPENSATED`, `CANCELLED` 등으로 관리 |
| (선택) `product_stock_reservation` | `product_id`, `order_id`, `reserved_quantity`, `status`, `expires_at`, `created_at`, `updated_at` | 다중 단계(예약→차감→확정)가 필요한 경우 사용. 단순 차감 시 Ledger 만으로 처리 가능 |

`ProductStockLedger` 상태 전이 규칙:
1. **RELEASE 요청**  
   - `product_stock_ledger` 에 `adjustment_type = RELEASE`, `status = PENDING` 레코드 삽입.  
   - `product` 테이블에서 `stock = stock - quantity` 를 시도하며 `version` 비교로 충돌 시 재시도.  
   - 성공 시 `status = COMPLETED` 로 갱신. 이미 동일 orderId/RELEASE 레코드가 `COMPLETED` 라면 멱등 처리(`PRODUCT_STOCK_ALREADY_RELEASED`).  
   - 부족한 경우 `status = CANCELLED` 로 기록하고 400 + `PRODUCT_STOCK_NOT_ENOUGH` 반환.
2. **RETURN 요청**  
   - 기존 RELEASE 레코드를 조회하여 `COMPLETED` 상태인지 확인.  
   - 존재하지 않으면 400 + `PRODUCT_STOCK_NOT_RESERVED`.  
   - 이미 RETURN 이 완료된 경우 400 + `PRODUCT_STOCK_ALREADY_RETURNED`(추가 에러 코드) 반환.  
   - `product_stock_ledger` 에 `adjustment_type = RETURN`, `status = PENDING` 레코드 삽입 후 `stock = stock + quantity` 업데이트, 완료 시 `status = COMPLETED` 로 변경하고 RELEASE 레코드를 `COMPENSATED` 로 마크한다.

만약 주문 생성 이후 일정 시간 내에 결제가 완료되지 않으면 재고를 자동 복원해야 한다면, `expired_at` 기반으로 배치/스케줄러가 PENDING/COMPLETED 상태를 검사해 반환 처리할 수 있도록 설계한다.

#### 서비스 호출에 대한 트랜잭션/락 전략
- **낙관적 락**: `@Version` 컬럼을 두고 실패 시 재시도. 트래픽이 높지 않다면 간단하고 중복 호출 방지에 충분하다.
- **비관적 락**: `SELECT ... FOR UPDATE` 로 row-level lock 을 걸어 다중 노드에서 동시에 차감하는 상황을 더 확실히 제어할 수 있다. Ledger 기록과 함께 사용 시 가장 안전하지만 트래픽이 많을 경우 주의.
- `Resilience4j` Retry 와 함께 사용할 경우, Product 서비스 내부에서는 재시도 횟수를 제한해 데드락을 줄인다.

#### API ↔ 도메인 매핑
- `stock-release` 성공 → Ledger `RELEASE/COMPLETED` + Product.stock 감소.
- `stock-return` 성공 → Ledger `RETURN/COMPLETED` + Product.stock 증가 + 기존 RELEASE 레코드 `COMPENSATED`.
- `stock-release` 실패(재고 부족) → Ledger `RELEASE/CANCELLED` 저장 후 Product.stock 변경 없음.
- `stock-return` 실패(기록 없음) → Ledger 미삽입, 400 + 에러 코드.

## Order 서비스 (`com.example.shop.order`)

### 퍼블릭 API (`/v1/orders`)

| 메서드 | 엔드포인트 | 설명 | 쿼리/바디 | 정상 응답 | 주요 에러 코드(HTTP 4xx) |
| --- | --- | --- | --- | --- | --- |
| GET | `/v1/orders` | 주문 목록 조회 | `page`, `size`, `sort` | 200 + `orderPage`(status, totalAmount 등) | `ORDER_FORBIDDEN` |
| GET | `/v1/orders/{id}` | 주문 상세 조회 | - | 200 + 주문, 주문상품, 결제 요약 | `ORDER_NOT_FOUND`, `ORDER_FORBIDDEN` |
| POST | `/v1/orders` | 주문 생성 | `{"order": {"orderItemList": [{"productId": "...", "quantity": 1+}, ...]}}` | 200 + 생성된 주문 요약(상품 목록/총액) | `ORDER_BAD_REQUEST`, `ORDER_PRODUCT_NOT_FOUND`, `ORDER_PRODUCT_OUT_OF_STOCK` |
| POST | `/v1/orders/{id}/cancel` | 주문 취소 | - | 200 | `ORDER_NOT_FOUND`, `ORDER_ALREADY_CANCELLED`, `ORDER_FORBIDDEN` |

> 권한: 일반 사용자는 본인 주문만 조회/취소 가능, ADMIN/MANAGER 는 전체 조회/취소 가능.

### Product 서비스 호출 규칙
1. `POST /v1/orders` 처리 중 각 주문 상품에 대해 순차 또는 배치로 `POST /internal/v1/products/{productId}/stock-release` 호출.
2. 호출 한 건이라도 실패하면, 이미 성공한 항목을 역순으로 `POST /internal/v1/products/{productId}/stock-return` 호출한 뒤 주문을 롤백한다.
3. 주문 취소(`POST /v1/orders/{id}/cancel`) 시 등록된 모든 주문 아이템에 대해 `stock-return` 호출 후 주문 상태를 `CANCELLED` 로 갱신한다.
4. `orderId` 는 주문 서비스가 생성한 UUID 를 사용하고, Product 측 Ledger/Reservation 과 매핑하여 멱등성·중복 방지를 구현한다.

### 에러 처리
- Product 호출 실패 시 Order 서비스는 Product 의 `ApiDto.code` 를 해석해 사용자/시스템 메시지를 결정한다.  
  예) `PRODUCT_STOCK_NOT_ENOUGH` → 주문 생성 실패, 주문 레코드 저장 금지.
- Order 서비스 자체 오류는 기존 `OrderError` 코드를 유지(HTTP 400/403/404).
- 보상 호출이 실패할 경우를 대비해 Retry/CircuitBreaker 전략을 Resilience4j 로 명시한다. 보상 실패가 반복될 경우 DB 트랜잭션에 보상 상태를 기록하고 운영 경고를 발생시키는 절차가 필요하다.

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
1. Product Ledger/Reservation 설계 확정: 엔티티 스키마, 인덱스, 상태 머신 정의.
2. 재고 차감/복원 시나리오 테스트: WireMock/Testcontainers 등으로 멀티 서비스 통합 테스트 전략을 수립(후속 작업).
3. Gateway 레벨 CircuitBreaker/Rate Limiter/모니터링 정책 확정: JWT 검증 필터는 구축되어 있으나 탄력성·관찰성 구성이 필요하다.
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
- Product 내부 API(`stock-release`, `stock-return`) 는 현재 더미 응답이며 멱등성 검증/재고 처리 로직 확정이 필요하다.
- Gateway 에는 공통 로깅, 모니터링, 서킷 브레이커, Rate Limiter 등 보강 과제가 남아 있다.
- RestDocs → OpenAPI 산출물 경로: 각 서비스 `build/api-spec/` 확인. 중앙 문서화/배포 정책은 추후 결정.
