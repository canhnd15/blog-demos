package com.davidnguyenblog.order.order;

public record CreateOrderRequest(long userId, String cartId, String sku, int qty, long amount) {

    /** Chuỗi chuẩn hóa để băm (hash) nội dung request */
    public String canonical() {
        return userId + "|" + cartId + "|" + sku + "|" + qty + "|" + amount;
    }
}
