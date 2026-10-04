package com.davidnguyenblog.inventory.inventory;

import com.davidnguyenblog.inventory.exception.OutOfStockException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Cach 1: doc roi tru (naive). Chi de minh hoa oversell, dung dung cho hang co gioi han.
@Service
@RequiredArgsConstructor
public class NaiveInventoryService {

    private final InventoryRepository inventoryRepository;

    // Dung dung: check-then-act race condition
    @Transactional
    public void reserve(String sku, int qty) {
        Inventory inv = inventoryRepository.findById(sku).orElseThrow();

        if (inv.getAvailable() < qty) {
            throw new OutOfStockException(sku);
        }

        inv.setAvailable(inv.getAvailable() - qty);
        inventoryRepository.save(inv);
    }
}
