package com.davidnguyenblog.order.order;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cách 2: check-then-insert. Có race condition, chỉ để minh họa. */
@Service
@RequiredArgsConstructor
public class NaiveOrderService {

    private final OrderRepository orderRepository;
    private final OrderCreator creator;

    // Đừng dùng: giữa SELECT và INSERT có khoảng hở
    @Transactional
    public Order create(CreateOrderRequest req) {
        List<Order> existing = orderRepository.findByUserIdAndCartId(req.userId(), req.cartId());
        if (!existing.isEmpty()) {
            return existing.get(0);
        }
        return creator.create(req, null);
    }
}
