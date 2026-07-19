package com.acme.auctions.core.auctioning.exception;

import com.acme.auctions.core.auctioning.model.AuctionId;

public final class AuctionConcurrencyConflictException extends AuctionBusinessException {

    public AuctionConcurrencyConflictException(AuctionId auctionId) {
        super("auction state changed concurrently: " + auctionId);
    }
}
