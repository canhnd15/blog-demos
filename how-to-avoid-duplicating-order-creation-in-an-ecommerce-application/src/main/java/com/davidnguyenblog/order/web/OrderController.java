package com.davidnguyenblog.order.web;

import com.davidnguyenblog.order.idempotency.IdempotentOrderService;
import com.davidnguyenblog.order.order.CreateOrderRequest;
import com.davidnguyenblog.order.order.Order;
import com.davidnguyenblog.order.order.UniqueKeyOrderService;
import com.davidnguyenblog.order.redis.RedisGatedOrderService;
import com.davidnguyenblog.order.token.CheckoutTokenService;
import com.davidnguyenblog.order.token.TokenOrderService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final UniqueKeyOrderService uniqueKeyService;
    private final IdempotentOrderService idempotentService;
    private final RedisGatedOrderService gatedService;
    private final CheckoutTokenService tokenService;
    private final TokenOrderService tokenOrderService;

    @PostMapping("/unique")
    Order unique(@RequestHeader("Idempotency-Key") String key, @RequestBody CreateOrderRequest req) {
        return uniqueKeyService.create(req, key);
    }

    @PostMapping("/idempotent")
    Order idempotent(@RequestHeader("Idempotency-Key") String key, @RequestBody CreateOrderRequest req) {
        return idempotentService.create(req, key);
    }

    @PostMapping("/gated")
    Order gated(@RequestHeader("Idempotency-Key") String key, @RequestBody CreateOrderRequest req) {
        return gatedService.create(req, key);
    }

    @PostMapping("/checkout-token")
    Map<String, String> issueToken(@RequestParam long userId, @RequestParam String cartId) {
        return Map.of("token", tokenService.issue(userId, cartId));
    }

    @PostMapping("/with-token")
    Order withToken(@RequestHeader("Checkout-Token") String token, @RequestBody CreateOrderRequest req) {
        return tokenOrderService.create(req, token);
    }
}
