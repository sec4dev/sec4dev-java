package com.sec4dev;

/** 500+ - Server errors. */
public class ServerException extends Sec4DevException {
    public ServerException(String message, int statusCode, Object responseBody) {
        super(message, statusCode, responseBody);
    }
}
