package com.acme.auctions.core.auctioning.exception;

public final class AuctionEndTimeMustBeInFutureException extends AuctionBusinessException {

    public AuctionEndTimeMustBeInFutureException() {
        super("auction end must be in the future");
    }
}
