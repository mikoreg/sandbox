package com.acme.auctions.core.auctioning.port.out;

import com.acme.auctions.core.auctioning.model.Auction;
import com.acme.auctions.core.auctioning.model.AuctionId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AuctionRepository {
    Auction save(Auction auction);

    Optional<Auction> findById(AuctionId auctionId);

    List<Auction> findExpiredOpenAuctions(Instant currentTime);
}
