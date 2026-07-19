package com.acme.auctions.core.auctioning.exception;

public abstract class AuctionBusinessException extends RuntimeException {

    protected AuctionBusinessException(String message) {
        super(message);
    }
}
