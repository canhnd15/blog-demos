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

/** Cách 5: Redis SET NX EX chặn sớm request trùng, DB (cách 3) vẫn là chốt chặn cuối. */
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
            // Request trùng: nếu order đã có thì trả về, chưa có thì báo đang xử lý
            return orderRepository.findByIdempotencyKey(key)
                    .orElseThrow(() -> new RequestInProgressException("Order dang duoc xu ly"));
        }
        try {
            return dbService.create(req, key);
        } catch (RuntimeException ex) {
            redis.delete(redisKey); // thất bại thì nhả key để client retry được
            throw ex;
        }
    }
}
