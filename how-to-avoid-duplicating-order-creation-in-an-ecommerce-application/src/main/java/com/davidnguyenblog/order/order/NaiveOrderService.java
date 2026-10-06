package com.davidnguyenblog.order.order;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cach 2: check-then-insert. Co race condition, chi de minh hoa. */
@Service
@RequiredArgsConstructor
public class NaiveOrderService {

    private final OrderRepository orderRepository;
    private final OrderCreator creator;

    // Dung dung: giua SELECT va INSERT co khoang ho
    @Transactional
    public Order create(CreateOrderRequest req) {
        List<Order> existing = orderRepository.findByUserIdAndCartId(req.userId(), req.cartId());
        if (!existing.isEmpty()) {
            return existing.get(0);
        }
        return creator.create(req, null);
    }
}
