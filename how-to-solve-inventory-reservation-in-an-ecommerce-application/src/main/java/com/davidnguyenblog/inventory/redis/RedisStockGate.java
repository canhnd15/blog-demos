package com.davidnguyenblog.inventory.redis;

import java.util.List;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

// Cach 5: Redis + Lua script lam cong chan phia truoc database.
@Service
public class RedisStockGate {

    private final StringRedisTemplate redis;
    private final DefaultRedisScript<Long> reserveScript;

    public RedisStockGate(StringRedisTemplate redis) {
        this.redis = redis;
        this.reserveScript = new DefaultRedisScript<>();
        // Doc file src/main/resources/scripts/reserve_stock.lua
        this.reserveScript.setLocation(new ClassPathResource("scripts/reserve_stock.lua"));
        this.reserveScript.setResultType(Long.class);
    }

    /** Nap ton kho tu DB len Redis truoc gio mo ban */
    public void warmUp(String sku, int available) {
        redis.opsForValue().set(key(sku), String.valueOf(available));
    }

    public boolean tryReserve(String sku, int qty) {
        Long result = redis.execute(reserveScript, List.of(key(sku)), String.valueOf(qty));
        if (result == null || result == -1L) {
            throw new IllegalStateException("Stock cua " + sku + " chua duoc warm-up");
        }
        return result == 1L;
    }

    /** Hoan lai khi buoc sau (ghi DB) that bai */
    public void rollback(String sku, int qty) {
        redis.opsForValue().increment(key(sku), qty);
    }

    private String key(String sku) {
        return "stock:" + sku;
    }
}
