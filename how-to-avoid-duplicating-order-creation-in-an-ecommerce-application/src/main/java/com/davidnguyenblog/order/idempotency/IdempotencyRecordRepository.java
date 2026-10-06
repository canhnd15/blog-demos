package com.davidnguyenblog.order.idempotency;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, String> {

    // 1 = minh la nguoi dau tien, 0 = key da ton tai
    @Modifying
    @Query(nativeQuery = true, value = """
        INSERT INTO idempotency_record (idempotency_key, user_id, request_hash, created_at)
        VALUES (:key, :userId, :hash, now())
        ON CONFLICT (idempotency_key) DO NOTHING
        """)
    int insertIfAbsent(@Param("key") String key, @Param("userId") long userId, @Param("hash") String hash);

    @Modifying
    @Query(nativeQuery = true, value = """
        UPDATE idempotency_record SET order_id = :orderId WHERE idempotency_key = :key
        """)
    int attachOrder(@Param("key") String key, @Param("orderId") UUID orderId);
}
