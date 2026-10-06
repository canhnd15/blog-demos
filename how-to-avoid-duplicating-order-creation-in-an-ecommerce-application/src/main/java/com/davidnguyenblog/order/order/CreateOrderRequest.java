package com.davidnguyenblog.order.order;

public record CreateOrderRequest(long userId, String cartId, String sku, int qty, long amount) {

    /** Chuoi chuan hoa de bam (hash) noi dung request */
    public String canonical() {
        return userId + "|" + cartId + "|" + sku + "|" + qty + "|" + amount;
    }
}
