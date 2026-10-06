package com.davidnguyenblog.order.redis;

import com.davidnguyenblog.order.order.CreateOrderRequest;
import com.davidnguyenblog.order.order.Order;
import com.davidnguyenblog.order.order.OrderRepository;
import com.davidnguyenblog.order.order.UniqueKeyOrderService;
import com.davidnguyenblog.order.web.RequestInProgressException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/** Cach 5: Redis SET NX EX chan som request trung, DB (cach 3) van la chot chan cuoi. */
@Service
@RequiredArgsConstructor
public class RedisGatedOrderService {

    private static final Duration WINDOW = Duration.ofMinutes(10);

    private final StringRedisTemplate redis;
    private final UniqueKeyOrderService dbService;
    private final OrderRepository orderRepository;

    public Order create(CreateOrderRequest req, String key) {
        String redisKey = "order-dedupe:" + req.userId() + ":" + key;

        Boolean acquired = redis.opsForValue().setIfAbsent(redisKey, "PROCESSING", WINDOW);
        if (!Boolean.TRUE.equals(acquired)) {
            // Request trung: neu order da co thi tra ve, chua co thi bao dang xu ly
            return orderRepository.findByIdempotencyKey(key)
                    .orElseThrow(() -> new RequestInProgressException("Order dang duoc xu ly"));
        }
        try {
            return dbService.create(req, key);
        } catch (RuntimeException ex) {
            redis.delete(redisKey); // that bai thi nha key de client retry duoc
            throw ex;
        }
    }
}
