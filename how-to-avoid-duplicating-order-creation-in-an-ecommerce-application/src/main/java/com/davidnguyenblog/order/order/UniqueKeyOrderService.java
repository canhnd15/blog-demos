package com.davidnguyenblog.order.order;

import com.davidnguyenblog.order.web.IdempotencyConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/** Cách 3: Idempotency-Key + UNIQUE constraint, insert-and-catch. */
@Service
@RequiredArgsConstructor
public class UniqueKeyOrderService {

    private final OrderCreator creator;
    private final OrderRepository orderRepository;

    // Cố ý KHÔNG có @Transactional: transaction của creator.create() đã rollback khi tới được catch
    public Order create(CreateOrderRequest req, String key) {
        try {
            return creator.create(req, key);
        } catch (DataIntegrityViolationException ex) {
            // Request khác cùng key đã thắng, trả về order của nó
            Order existing = orderRepository.findByIdempotencyKey(key).orElseThrow(() -> ex);
            if (existing.getUserId() != req.userId()) {
                throw new IdempotencyConflictException("Key da duoc dung boi user khac");
            }
            return existing;
        }
    }
}
