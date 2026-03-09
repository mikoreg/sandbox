package com.acme.auctions.core.auctioning.port.in;

import com.acme.auctions.core.auctioning.model.AuctionId;

public interface FindAuctionDetailsUseCase {
    AuctionDetailsResult findAuctionDetails(AuctionId auctionId);
}
