package com.acme.auctions.core.auctioning.exception;

public final class AuctionClosedForBiddingException extends AuctionBusinessException {

    public AuctionClosedForBiddingException() {
        super("auction is already closed");
    }
}
