# Order duplication demo

Code di kem bai viet "Lam sao tranh tao trung Order khi flash sale trong he thong e-commerce?" tren [davidnguyen.blog](https://davidnguyenblog.vercel.app/blog/how-to-avoid-duplicating-order-creation-in-an-ecommerce-application).

Project cai dat cac cach chong tao trung order duoc phan tich trong bai (cach 1 la nut bam o client nen khong co code):

2. `NaiveOrderService` - check-then-insert (co race condition, chi de minh hoa)
3. `UniqueKeyOrderService` - Idempotency-Key + UNIQUE constraint, insert-and-catch
4. `IdempotentOrderService` - bang `idempotency_record` (key, request hash, order)
5. `RedisGatedOrderService` - Redis SET NX EX chan som, DB van la chot chan cuoi
6. `CheckoutTokenService` + `TokenOrderService` - token dung mot lan do server cap (Redis GETDEL)

## Yeu cau

- Java 21+
- Maven 3.9+
- Docker (cho PostgreSQL va Redis)

## Chay thu

```bash
# 1. Khoi dong PostgreSQL (cong 5433) + Redis (cong 6379)
docker compose up -d --wait

# 2. Ban 50 thread cung luc vao moi cach
mvn test
```

Ket qua mong doi (so don o cach 2 thay doi moi lan chay, cac cach con lai luon la 1):

```
[naive] responses=50, orders in DB=30
[unique] responses=50, distinct ids=1, orders in DB=1
[idempotent] responses=50, distinct ids=1, orders in DB=1
[token] responses=34, distinct ids=1, orders in DB=1
[gated] responses=46, distinct ids=1, orders in DB=1
```

Cach 5 va 6 co so response < 50 vi mot so request den khi order dang duoc ghi nen bi tu choi (409 hoac 400), client retry sau la duoc.

## Chay API

```bash
mvn spring-boot:run

curl -X POST localhost:8080/api/orders/idempotent \
  -H 'Content-Type: application/json' -H 'Idempotency-Key: abc-123' \
  -d '{"userId":1,"cartId":"cart-1","sku":"HEADPHONE-01","qty":1,"amount":99000}'
```

Goi lai cung lenh se tra ve cung mot order. Doi noi dung body nhung giu key se nhan 422.

## Don dep

```bash
docker compose down -v
```
