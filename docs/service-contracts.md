# 서비스 인터페이스 및 상호 연동 정리

본 문서는 `example-shop-springboot-msa-mark1` 전환 작업을 위한 Product, Order 서비스의 REST 계약과 내부 연동 규칙을 정의한다. 모든 응답은 공통 모듈의 `ApiDto<T>` 포맷을 따른다.

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

## Gateway 인증/캐시 전략
- Gateway 는 모든 요청을 수신하면 `Authorization` 헤더의 액세스 토큰 문자열을 `SHA-256(token + salt)` 로 해시하여 카페인 캐시 키로 사용한다. 원문 토큰은 캐시에 저장하지 않는다.
- 캐시 value 는 화이트리스트 `0`, 블랙리스트 `1` 의 정수 값만 사용한다. 추가 정보(사용자 ID 등)는 캐시에 보관하지 않는다.
- 캐시 미스 발생 시 user(auth) 서비스의 `POST /v1/auth/access-token-check` 엔드포인트로 검증을 위임한다.  
  - 요청 바디: `{"accessJwt": "<jwt 문자열>"}`  
  - 응답: `ApiDto`(`code = AUTH_TOKEN_VALID` 등) 로 서명 검증 결과, 남은 TTL(초), JWT `exp`, `jwtValidator` 타임스탬프 등을 반환한다.
- 검증 성공 시 Gateway 는 `(exp - now - 5초)` 를 계산하여 화이트리스트 캐시 TTL 로 설정한다. 남은 유효 시간이 5초 이하일 경우 캐시에 저장하지 않고 한 번만 통과시킨다.
- user 서비스는 모놀리식과 동일하게 사용자 레코드에 `jwtValidator` 타임스탬프를 유지하며, 검증 응답 시 `jwtValidator + accessTokenValiditySeconds` 를 함께 전달한다. Gateway 는 블랙리스트 캐시 TTL 을 이 값으로 설정해 만료 전까지 요청을 차단한다.
- 블랙리스트 등록은 user 서비스에서 토큰 무효화 시 Gateway 로 이벤트/요청을 보내 캐시에 `value = 1` 로 저장하도록 구현한다. 해당 TTL 이 경과하면 자동으로 삭제된다.
- 캐시 히트 시에는 user 서비스 호출 없이 곧바로 다음 서비스로 요청을 전달하며, 이후 각 서비스는 전달된 토큰의 페이로드(클레임)만 읽어 인증/인가를 수행한다.

> user 서비스 검증 API 가 실패하거나 타임아웃 될 경우 대비해 Gateway 에서는 Resilience4j CircuitBreaker + Retry, 타임아웃 정책을 설정한다. 실패 시 기본 응답 코드는 503 또는 401 로 통일한다.

## 공통 정책 요약
- 모든 서비스는 `/v1` 로 시작하는 버전명을 사용한다. 내부 전용 엔드포인트는 `/internal/v1` 네임스페이스를 사용해 Gateway 외부 노출을 차단한다.
- HTTP 200/400 중심으로 상태 코드를 단순화하고, 상세 오류는 `ApiDto.code` 와 `message` 로 전달한다.
- Zipkin trace id 는 `X-B3-TraceId` 등 Spring Cloud Sleuth 기본 헤더로 전파한다.
- RestTemplate 호출에는 Resilience4j CircuitBreaker + Retry 를 적용하고, 타임아웃 및 fallback 정책을 `application.yml` 에 정의한다.

## 향후 확인 필요 사항
1. Product Ledger/Reservation 설계 확정: 엔티티 스키마, 인덱스, 상태 머신 정의.
2. 재고 차감/복원 시나리오 테스트: WireMock/Testcontainers 등으로 멀티 서비스 통합 테스트 전략을 수립(후속 작업).

## 진행 메모 (미완료/추가 확인)
- user 인증 API `/v1/auth/access-token-check` 응답 필드에서 `jwtValidatorTimestamp` 를 제거했음. 실제 구현 시 캐시 TTL 산출 로직과 문서 동기화 필요.
- Product 내부 API(`stock-release`, `stock-return`) 는 현재 더미 응답이며 멱등성 검증/재고 처리 로직 확정이 필요하다.
- RestDocs → OpenAPI 산출물 경로: 각 서비스 `build/api-spec/` 확인. 중앙 문서화/배포 정책은 추후 결정.
