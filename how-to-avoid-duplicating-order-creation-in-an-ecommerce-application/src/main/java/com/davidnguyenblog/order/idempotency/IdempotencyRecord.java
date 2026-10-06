package com.davidnguyenblog.order.idempotency;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "idempotency_record")
@Getter
@Setter
public class IdempotencyRecord {

    @Id
    @Column(name = "idempotency_key")
    private String key;

    @Column(name = "user_id", nullable = false)
    private long userId;

    @Column(name = "request_hash", nullable = false)
    private String requestHash;

    @Column(name = "order_id")
    private UUID orderId;
}
