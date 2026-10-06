package com.davidnguyenblog.order.web;

public class RequestInProgressException extends RuntimeException {
    public RequestInProgressException(String message) {
        super(message);
    }
}
