# Inventory Reservation demo

Code di kem bai viet [Làm sao xử lý Inventory Reservation khi flash sale mà không bị oversell?](https://davidnguyenblog.vercel.app/blog/how-to-solve-inventory-reservation-in-an-ecommerce-application) tren [davidnguyen.blog](https://davidnguyenblog.vercel.app).

Project nay cai dat ca 6 cach tiep can duoc phan tich trong bai, tren cung mot bang `inventory`:

1. `NaiveInventoryService` - doc roi tru (co bug oversell, chi de minh hoa)
2. `AtomicInventoryService` - atomic UPDATE co dieu kien
3. `PessimisticInventoryService` - `SELECT ... FOR UPDATE`
4. `OptimisticInventoryService` + `InventoryTxService` - optimistic lock voi `@Version`
5. `RedisStockGate` + `GatedInventoryService` - Redis Lua script lam cong chan phia truoc
6. `ReservationService` / `ReservationFacade` / `ReservationExpiryJob` - reservation co TTL

## Yeu cau

- Java 21+
- Maven 3.9+
- Docker (cho PostgreSQL va Redis)

## Chay thu

```bash
# 1. Khoi dong PostgreSQL + Redis
docker compose up -d

# 2. Chay test minh hoa khong oversell (ban 200 thread vao kho co 100 san pham)
mvn test
```

Ket qua mong doi:

```
success=100, soldOut=100, errors=0, available=0
```

## Tai hien loi oversell

Mo `src/test/java/.../InventoryConcurrencyTest.java`, doi:

```java
@Autowired AtomicInventoryService service;
```

thanh:

```java
@Autowired NaiveInventoryService service;
```

roi chay lai `mvn test`. Assertion `success + left == 100` se fail va `available` co the am hoac khong khop so don thanh cong - dung bug duoc mo ta trong bai (Cach 1).

## Don dep

```bash
docker compose down -v
```

## Lien quan

- Bai goc: https://davidnguyenblog.vercel.app/blog/how-to-solve-inventory-reservation-in-an-ecommerce-application
- Bai ve duplicate username (cung ky thuat "day dam bao dung dan xuong tang du lieu"): https://davidnguyenblog.vercel.app/blog/how-to-prevent-duplicate-username-at-scale
