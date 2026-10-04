package com.davidnguyenblog.inventory.reservation;

import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationExpiryJob {

    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;

    // Can @EnableScheduling o mot class @Configuration (xem InventoryDemoApplication)
    @Scheduled(fixedDelay = 30_000)
    public void releaseExpired() {
        List<StockReservation> expired =
                reservationRepository.findTop100ByStatusAndExpiresAtBefore("HELD", Instant.now());

        for (StockReservation r : expired) {
            reservationService.release(r, "EXPIRED");
        }
    }
}
