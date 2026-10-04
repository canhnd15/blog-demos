package com.davidnguyenblog.inventory.inventory;

import com.davidnguyenblog.inventory.exception.OutOfStockException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Cach 2: atomic UPDATE co dieu kien. Diem bat dau cho hau het he thong.
@Service
@RequiredArgsConstructor
public class AtomicInventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional
    public void reserve(String sku, int qty) {
        int updated = inventoryRepository.decreaseIfEnough(sku, qty);
        if (updated == 0) {
            throw new OutOfStockException(sku);
        }
    }
}
