package com.davidnguyenblog.order.order;

import com.davidnguyenblog.order.web.IdempotencyConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/** Cach 3: Idempotency-Key + UNIQUE constraint, insert-and-catch. */
@Service
@RequiredArgsConstructor
public class UniqueKeyOrderService {

    private final OrderCreator creator;
    private final OrderRepository orderRepository;

    // Co y KHONG co @Transactional: transaction cua creator.create() da rollback khi toi duoc catch
    public Order create(CreateOrderRequest req, String key) {
        try {
            return creator.create(req, key);
        } catch (DataIntegrityViolationException ex) {
            // Request khac cung key da thang, tra ve order cua no
            Order existing = orderRepository.findByIdempotencyKey(key).orElseThrow(() -> ex);
            if (existing.getUserId() != req.userId()) {
                throw new IdempotencyConflictException("Key da duoc dung boi user khac");
            }
            return existing;
        }
    }
}
