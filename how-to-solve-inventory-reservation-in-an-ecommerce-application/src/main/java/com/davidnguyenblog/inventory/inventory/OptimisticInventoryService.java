package com.davidnguyenblog.inventory.inventory;

import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

// Cach 4: optimistic lock voi @Version. Hop khi xung dot hiem; khong phu hop cho SKU nong.
@Service
@RequiredArgsConstructor
public class OptimisticInventoryService {

    private static final int MAX_RETRY = 3;

    private final InventoryTxService txService;

    public void reserve(String sku, int qty) {
        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            try {
                txService.decrease(sku, qty);
                return;
            } catch (ObjectOptimisticLockingFailureException ex) {
                // Co request khac sua dong truoc minh, doc lai roi thu lan nua
                if (attempt == MAX_RETRY) {
                    throw ex;
                }
            }
        }
    }
}
