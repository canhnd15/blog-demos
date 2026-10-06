package com.davidnguyenblog.order;

import static org.assertj.core.api.Assertions.assertThat;

import com.davidnguyenblog.order.idempotency.IdempotentOrderService;
import com.davidnguyenblog.order.order.CreateOrderRequest;
import com.davidnguyenblog.order.order.NaiveOrderService;
import com.davidnguyenblog.order.order.Order;
import com.davidnguyenblog.order.order.OrderRepository;
import com.davidnguyenblog.order.order.UniqueKeyOrderService;
import com.davidnguyenblog.order.redis.RedisGatedOrderService;
import com.davidnguyenblog.order.token.CheckoutTokenService;
import com.davidnguyenblog.order.token.TokenOrderService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class OrderDuplicationTest {

    private static final int THREADS = 50;
    private static final CreateOrderRequest REQ =
            new CreateOrderRequest(1L, "cart-1", "HEADPHONE-01", 1, 99_000);

    @Autowired NaiveOrderService naive;
    @Autowired UniqueKeyOrderService unique;
    @Autowired IdempotentOrderService idempotent;
    @Autowired RedisGatedOrderService gated;
    @Autowired CheckoutTokenService tokenService;
    @Autowired TokenOrderService tokenOrders;
    @Autowired OrderRepository orderRepository;
    @Autowired JdbcTemplate jdbc;
    @Autowired StringRedisTemplate redis;

    @BeforeEach
    void clean() {
        jdbc.execute("TRUNCATE orders, idempotency_record");
        Set<String> keys = redis.keys("*");
        if (keys != null && !keys.isEmpty()) {
            redis.delete(keys);
        }
    }

    /** Bắn THREADS request cùng lúc, trả về các kết quả (exception được bỏ qua nhưng đếm lại) */
    private List<Order> fire(Callable<Order> call) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Order>> futures = new ArrayList<>();
        for (int i = 0; i < THREADS; i++) {
            futures.add(pool.submit(() -> {
                start.await();
                try {
                    return call.call();
                } catch (Exception e) {
                    return null; // vd: 409 đang xử lý ở cách 5
                }
            }));
        }
        start.countDown();
        List<Order> results = new ArrayList<>();
        for (Future<Order> f : futures) {
            Order o = f.get();
            if (o != null) results.add(o);
        }
        pool.shutdown();
        return results;
    }

    private Set<UUID> distinctIds(List<Order> orders) {
        Set<UUID> ids = new HashSet<>();
        orders.forEach(o -> ids.add(o.getId()));
        return ids;
    }

    @Test
    void naiveCreatesDuplicates() throws Exception {
        List<Order> results = fire(() -> naive.create(REQ));
        long inDb = orderRepository.countByUserId(1L);
        System.out.printf("[naive] responses=%d, orders in DB=%d%n", results.size(), inDb);
        // Không assert cứng: số đơn trùng thay đổi mỗi lần chạy. Mục tiêu là quan sát inDb > 1.
    }

    @Test
    void uniqueKeyCreatesOneOrder() throws Exception {
        List<Order> results = fire(() -> unique.create(REQ, "key-1"));
        long inDb = orderRepository.countByUserId(1L);
        System.out.printf("[unique] responses=%d, distinct ids=%d, orders in DB=%d%n",
                results.size(), distinctIds(results).size(), inDb);
        assertThat(inDb).isEqualTo(1);
        assertThat(results).hasSize(THREADS);
        assertThat(distinctIds(results)).hasSize(1);
    }

    @Test
    void idempotencyRecordCreatesOneOrder() throws Exception {
        List<Order> results = fire(() -> idempotent.create(REQ, "key-2"));
        long inDb = orderRepository.countByUserId(1L);
        System.out.printf("[idempotent] responses=%d, distinct ids=%d, orders in DB=%d%n",
                results.size(), distinctIds(results).size(), inDb);
        assertThat(inDb).isEqualTo(1);
        assertThat(results).hasSize(THREADS);
        assertThat(distinctIds(results)).hasSize(1);
    }

    @Test
    void redisGateCreatesOneOrder() throws Exception {
        List<Order> results = fire(() -> gated.create(REQ, "key-3"));
        long inDb = orderRepository.countByUserId(1L);
        System.out.printf("[gated] responses=%d, distinct ids=%d, orders in DB=%d%n",
                results.size(), distinctIds(results).size(), inDb);
        assertThat(inDb).isEqualTo(1);
        assertThat(distinctIds(results)).hasSize(1);
    }

    @Test
    void checkoutTokenCreatesOneOrder() throws Exception {
        String token = tokenService.issue(1L, "cart-1");
        List<Order> results = fire(() -> tokenOrders.create(REQ, token));
        long inDb = orderRepository.countByUserId(1L);
        System.out.printf("[token] responses=%d, distinct ids=%d, orders in DB=%d%n",
                results.size(), distinctIds(results).size(), inDb);
        assertThat(inDb).isEqualTo(1);
        assertThat(distinctIds(results)).hasSize(1);
    }

    @Test
    void forgedTokenIsRejected() {
        boolean ok = tokenService.consume("forged", 1L, "cart-1");
        assertThat(ok).isFalse();
    }
}
