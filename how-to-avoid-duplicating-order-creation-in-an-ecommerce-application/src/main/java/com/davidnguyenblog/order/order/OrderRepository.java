package com.davidnguyenblog.order.order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByUserIdAndCartId(long userId, String cartId);

    Optional<Order> findByIdempotencyKey(String idempotencyKey);

    long countByUserId(long userId);
}
