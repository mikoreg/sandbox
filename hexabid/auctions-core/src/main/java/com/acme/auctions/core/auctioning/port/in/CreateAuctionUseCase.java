package com.acme.auctions.core.auctioning.port.in;

public interface CreateAuctionUseCase {
    CreateAuctionResult createAuction(CreateAuctionCommand command);
}
