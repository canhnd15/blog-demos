package com.davidnguyenblog.inventory;

import static org.assertj.core.api.Assertions.assertThat;

import com.davidnguyenblog.inventory.exception.OutOfStockException;
import com.davidnguyenblog.inventory.inventory.AtomicInventoryService;
import com.davidnguyenblog.inventory.inventory.InventoryRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

// Doi AtomicInventoryService -> NaiveInventoryService de tai hien oversell (cach 1).
@SpringBootTest
class InventoryConcurrencyTest {

    @Autowired AtomicInventoryService service;
    @Autowired InventoryRepository inventoryRepository;
    @Autowired JdbcTemplate jdbcTemplate;

    // Dua kho ve 100 truoc moi lan chay de cac lan test khong anh huong nhau
    @BeforeEach
    void resetStock() {
        jdbcTemplate.update("UPDATE inventory SET available = 100 WHERE sku = 'HEADPHONE-01'");
    }

    @Test
    void shouldNotOversell() throws Exception {
        int threads = 200;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger soldOut = new AtomicInteger();
        AtomicInteger errors = new AtomicInteger();

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            futures.add(pool.submit(() -> {
                start.await();                       // cho tin hieu de cung chay
                try {
                    service.reserve("HEADPHONE-01", 1);
                    success.incrementAndGet();
                } catch (OutOfStockException e) {
                    soldOut.incrementAndGet();
                } catch (Exception e) {
                    // Loi khac (het connection, optimistic lock sau khi retry het luot, vi pham CHECK...)
                    errors.incrementAndGet();
                }
                return null;
            }));
        }

        start.countDown();
        for (Future<?> f : futures) f.get();
        pool.shutdown();

        int left = inventoryRepository.findById("HEADPHONE-01").orElseThrow().getAvailable();
        System.out.printf("success=%d, soldOut=%d, errors=%d, available=%d%n",
                success.get(), soldOut.get(), errors.get(), left);

        // Bat bien quan trong nhat: so don thanh cong va so hang con lai phai khop voi ton kho ban dau
        assertThat(success.get() + left).isEqualTo(100);
        assertThat(left).isGreaterThanOrEqualTo(0);
    }
}
