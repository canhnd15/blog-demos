package com.davidnguyenblog.inventory.reservation;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

// Facade ben ngoai transaction, bat loi trung order_id va tra ve reservation da co.
@Service
@RequiredArgsConstructor
public class ReservationFacade {

    private final ReservationService reservationService;
    private final ReservationRepository reservationRepository;

    // Co y KHONG co @Transactional: transaction cua reserve() da rollback khi toi duoc day
    public StockReservation reserve(String orderId, String sku, int qty) {
        try {
            return reservationService.reserve(orderId, sku, qty);
        } catch (DataIntegrityViolationException ex) {
            // Request khac cung orderId da thang, tra ve ket qua cua no (kho chi bi tru mot lan)
            return reservationRepository.findByOrderId(orderId).orElseThrow(() -> ex);
        }
    }
}
