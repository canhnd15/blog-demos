package com.davidnguyenblog.inventory.inventory;

import com.davidnguyenblog.inventory.exception.OutOfStockException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Cach 3: pessimistic lock (SELECT ... FOR UPDATE). Dung khi can doc truoc roi moi quyet dinh.
@Service
@RequiredArgsConstructor
public class PessimisticInventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional
    public void reserve(String sku, int qty) {
        // Tu day toi commit, khong ai khac sua duoc dong nay
        Inventory inv = inventoryRepository.findBySkuForUpdate(sku).orElseThrow();

        if (inv.getAvailable() < qty) {
            throw new OutOfStockException(sku);
        }
        inv.setAvailable(inv.getAvailable() - qty);
    }
}
