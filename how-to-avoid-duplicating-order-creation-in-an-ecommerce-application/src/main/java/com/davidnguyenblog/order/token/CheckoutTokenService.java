package com.davidnguyenblog.order.token;

import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/** Cách 6: token dùng một lần, do server cấp, gắn với user + cart. */
@Service
@RequiredArgsConstructor
public class CheckoutTokenService {

    private static final Duration TTL = Duration.ofMinutes(15);

    private final StringRedisTemplate redis;

    /** Gọi khi user mở trang checkout */
    public String issue(long userId, String cartId) {
        String token = UUID.randomUUID().toString();
        redis.opsForValue().set(key(token), userId + ":" + cartId, TTL);
        return token;
    }

    /** GETDEL: lấy và xóa trong một lệnh nguyên tử, chỉ một request được dùng token */
    public boolean consume(String token, long userId, String cartId) {
        String bound = redis.opsForValue().getAndDelete(key(token));
        return (userId + ":" + cartId).equals(bound);
    }

    private String key(String token) {
        return "checkout-token:" + token;
    }
}
