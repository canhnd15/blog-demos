package com.davidnguyenblog.inventory.exception;

public class OutOfStockException extends RuntimeException {
    public OutOfStockException(String sku) {
        super("SKU '" + sku + "' da het hang");
    }
}
