package com.davidnguyenblog.order.order;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Ghi một order trong một transaction. Các cách 2, 5, 6 dùng chung. */
@Service
@RequiredArgsConstructor
public class OrderCreator {

    private final OrderRepository orderRepository;

    @Transactional
    public Order create(CreateOrderRequest req, String idempotencyKey) {
        Order o = new Order();
        o.setUserId(req.userId());
        o.setCartId(req.cartId());
        o.setSku(req.sku());
        o.setQty(req.qty());
        o.setAmount(req.amount());
        o.setStatus("CREATED");
        o.setIdempotencyKey(idempotencyKey);
        o.setCreatedAt(Instant.now());
        // saveAndFlush để lỗi UNIQUE nổ ra ngay trong transaction này
        return orderRepository.saveAndFlush(o);
    }
}
