package com.acme.auctions.core.auctioning.exception;

public final class SellerCannotBidOnOwnAuctionException extends AuctionBusinessException {

    public SellerCannotBidOnOwnAuctionException() {
        super("seller cannot bid on own auction");
    }
}
