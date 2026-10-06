# Order duplication demo

Code đi kèm bài viết "Làm sao tránh tạo trùng Order khi flash sale trong hệ thống e-commerce?" trên [davidnguyen.blog](https://davidnguyenblog.vercel.app/blog/how-to-avoid-duplicating-order-creation-in-an-ecommerce-application).

Project cài đặt các cách chống tạo trùng order được phân tích trong bài (cách 1 là nút bấm ở client nên không có code):

2. `NaiveOrderService` - check-then-insert (có race condition, chỉ để minh họa)
3. `UniqueKeyOrderService` - Idempotency-Key + UNIQUE constraint, insert-and-catch
4. `IdempotentOrderService` - bảng `idempotency_record` (key, request hash, order)
5. `RedisGatedOrderService` - Redis SET NX EX chặn sớm, DB vẫn là chốt chặn cuối
6. `CheckoutTokenService` + `TokenOrderService` - token dùng một lần do server cấp (Redis GETDEL)

## Yêu cầu

- Java 21+
- Maven 3.9+
- Docker (cho PostgreSQL và Redis)

## Chạy thử

```bash
# 1. Khởi động PostgreSQL (cổng 5433) + Redis (cổng 6379)
docker compose up -d --wait

# 2. Bắn 50 thread cùng lúc vào mỗi cách
mvn test
```

Kết quả mong đợi (số đơn ở cách 2 thay đổi mỗi lần chạy, các cách còn lại luôn là 1):

```
[naive] responses=50, orders in DB=30
[unique] responses=50, distinct ids=1, orders in DB=1
[idempotent] responses=50, distinct ids=1, orders in DB=1
[token] responses=34, distinct ids=1, orders in DB=1
[gated] responses=46, distinct ids=1, orders in DB=1
```

Cách 5 và 6 có số response < 50 vì một số request đến khi order đang được ghi nên bị từ chối (409 hoặc 400), client retry sau là được.

## Chạy API

```bash
mvn spring-boot:run

curl -X POST localhost:8080/api/orders/idempotent \
  -H 'Content-Type: application/json' -H 'Idempotency-Key: abc-123' \
  -d '{"userId":1,"cartId":"cart-1","sku":"HEADPHONE-01","qty":1,"amount":99000}'
```

Gọi lại cùng lệnh sẽ trả về cùng một order. Đổi nội dung body nhưng giữ key sẽ nhận 422.

## Dọn dẹp

```bash
docker compose down -v
```
