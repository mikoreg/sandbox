package com.acme.auctions.core.auctioning.usecase;

import com.acme.auctions.core.auctioning.model.AuctionId;
import com.acme.auctions.core.auctioning.port.in.AuctionDetailsResult;
import com.acme.auctions.core.auctioning.port.in.FindAuctionDetailsUseCase;
import com.acme.auctions.core.auctioning.port.out.AuctionRepository;

import java.util.Objects;

public final class FindAuctionDetailsService implements FindAuctionDetailsUseCase {

    private final AuctionRepository auctionRepository;

    public FindAuctionDetailsService(AuctionRepository auctionRepository) {
        this.auctionRepository = Objects.requireNonNull(auctionRepository, "auctionRepository must not be null");
    }

    @Override
    public AuctionDetailsResult findAuctionDetails(AuctionId auctionId) {
        return auctionRepository.findById(auctionId)
                .<AuctionDetailsResult>map(auction -> new AuctionDetailsResult.AuctionFound(AuctionViews.from(auction)))
                .orElseGet(() -> new AuctionDetailsResult.AuctionMissing(auctionId));
    }
}
