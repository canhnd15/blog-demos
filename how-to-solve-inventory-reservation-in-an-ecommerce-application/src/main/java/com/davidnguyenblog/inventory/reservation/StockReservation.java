package com.davidnguyenblog.inventory.reservation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "stock_reservation")
@Getter
@Setter
public class StockReservation {

    @Id
    private UUID id;

    @Column(name = "order_id", nullable = false, unique = true)
    private String orderId;

    private String sku;

    private int qty;

    // HELD | CONFIRMED | RELEASED | EXPIRED
    private String status;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
}
