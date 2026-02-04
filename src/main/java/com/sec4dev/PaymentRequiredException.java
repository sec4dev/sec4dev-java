package com.sec4dev;

/** 402 - Quota exceeded. */
public class PaymentRequiredException extends Sec4DevException {
    public PaymentRequiredException(String message, int statusCode, Object responseBody) {
        super(message, statusCode, responseBody);
    }
}
