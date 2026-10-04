package com.davidnguyenblog.inventory.reservation;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<StockReservation, UUID> {

    Optional<StockReservation> findByOrderId(String orderId);

    List<StockReservation> findTop100ByStatusAndExpiresAtBefore(String status, Instant now);

    // Chuyen trang thai co dieu kien: chi mot ben "thang" khi confirm va expire dung nhau
    @Modifying
    @Query("UPDATE StockReservation r SET r.status = :to WHERE r.id = :id AND r.status = :from")
    int transition(@Param("id") UUID id, @Param("from") String from, @Param("to") String to);
}
