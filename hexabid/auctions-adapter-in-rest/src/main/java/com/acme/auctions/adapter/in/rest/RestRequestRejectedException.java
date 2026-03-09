package com.acme.auctions.adapter.in.rest;

import org.springframework.http.HttpStatus;

public class RestRequestRejectedException extends RuntimeException {

    private final HttpStatus status;

    public RestRequestRejectedException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }
}
