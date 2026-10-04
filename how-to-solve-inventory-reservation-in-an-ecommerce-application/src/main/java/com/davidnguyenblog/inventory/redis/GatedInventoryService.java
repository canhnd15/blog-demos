package com.davidnguyenblog.inventory.redis;

import com.davidnguyenblog.inventory.exception.OutOfStockException;
import com.davidnguyenblog.inventory.inventory.AtomicInventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GatedInventoryService {

    private final RedisStockGate gate;
    private final AtomicInventoryService dbService; // cach 2

    public void reserve(String sku, int qty) {
        if (!gate.tryReserve(sku, qty)) {
            // Chan ngay o Redis, khong cham database
            throw new OutOfStockException(sku);
        }
        try {
            dbService.reserve(sku, qty); // atomic UPDATE van la chot chan cuoi
        } catch (RuntimeException ex) {
            gate.rollback(sku, qty);     // ghi DB loi thi tra lai hang o Redis
            throw ex;
        }
    }
}
