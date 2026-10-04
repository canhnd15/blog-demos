package com.davidnguyenblog.inventory.inventory;

import com.davidnguyenblog.inventory.exception.OutOfStockException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryTxService {

    private final VersionedInventoryRepository versionedRepository;

    // Moi lan retry phai la mot transaction moi, nen tach thanh bean rieng
    @Transactional
    public void decrease(String sku, int qty) {
        VersionedInventory inv = versionedRepository.findById(sku).orElseThrow();

        if (inv.getAvailable() < qty) {
            throw new OutOfStockException(sku);
        }
        inv.setAvailable(inv.getAvailable() - qty);
        // Khi flush, Hibernate sinh: UPDATE ... WHERE sku = ? AND version = ?
        // Neu 0 dong bi anh huong => nem loi optimistic locking
    }
}
