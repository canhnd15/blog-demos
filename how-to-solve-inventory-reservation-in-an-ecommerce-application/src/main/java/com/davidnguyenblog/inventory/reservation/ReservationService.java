package com.davidnguyenblog.inventory.reservation;

import com.davidnguyenblog.inventory.exception.OutOfStockException;
import com.davidnguyenblog.inventory.inventory.InventoryRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Cach 6: reservation co TTL. Xay tren cach 2 (hoac cach 5), them vong doi + idempotency.
@Service
@RequiredArgsConstructor
public class ReservationService {

    private static final Duration HOLD_TIME = Duration.ofMinutes(10);

    private final InventoryRepository inventoryRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public StockReservation reserve(String orderId, String sku, int qty) {
        // Retry tuan tu cua cung mot don: tra lai ket qua cu, khong tru kho lan nua
        // (hai request dong thoi van co the lot qua day, constraint UNIQUE se chan o buoc save)
        Optional<StockReservation> existing = reservationRepository.findByOrderId(orderId);
        if (existing.isPresent()) {
            return existing.get();
        }

        // Tru kho bang atomic UPDATE o cach 2
        if (inventoryRepository.decreaseIfEnough(sku, qty) == 0) {
            throw new OutOfStockException(sku);
        }

        StockReservation r = new StockReservation();
        r.setId(UUID.randomUUID());
        r.setOrderId(orderId);
        r.setSku(sku);
        r.setQty(qty);
        r.setStatus("HELD");
        r.setExpiresAt(Instant.now().plus(HOLD_TIME));
        // saveAndFlush de loi UNIQUE no ra ngay tai day, trong transaction, thay vi luc commit
        return reservationRepository.saveAndFlush(r);
    }

    /** Goi khi payment thanh cong */
    @Transactional
    public void confirm(UUID reservationId) {
        int changed = reservationRepository.transition(reservationId, "HELD", "CONFIRMED");
        if (changed == 0) {
            throw new IllegalStateException("Reservation da het han hoac bi huy");
        }
    }

    /** Goi khi user huy don hoac job don dep phat hien qua han */
    @Transactional
    public void release(StockReservation r, String finalStatus) {
        // Chi khi chuyen trang thai thanh cong moi tra hang, nen chay lai bao nhieu lan cung an toan
        if (reservationRepository.transition(r.getId(), "HELD", finalStatus) == 1) {
            inventoryRepository.increase(r.getSku(), r.getQty());
        }
    }
}
