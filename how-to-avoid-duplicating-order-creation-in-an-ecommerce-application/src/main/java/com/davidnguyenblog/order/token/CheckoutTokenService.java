package com.davidnguyenblog.order.token;

import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/** Cach 6: token dung mot lan, do server cap, gan voi user + cart. */
@Service
@RequiredArgsConstructor
public class CheckoutTokenService {

    private static final Duration TTL = Duration.ofMinutes(15);

    private final StringRedisTemplate redis;

    /** Goi khi user mo trang checkout */
    public String issue(long userId, String cartId) {
        String token = UUID.randomUUID().toString();
        redis.opsForValue().set(key(token), userId + ":" + cartId, TTL);
        return token;
    }

    /** GETDEL: lay va xoa trong mot lenh nguyen tu, chi mot request duoc dung token */
    public boolean consume(String token, long userId, String cartId) {
        String bound = redis.opsForValue().getAndDelete(key(token));
        return (userId + ":" + cartId).equals(bound);
    }

    private String key(String token) {
        return "checkout-token:" + token;
    }
}
