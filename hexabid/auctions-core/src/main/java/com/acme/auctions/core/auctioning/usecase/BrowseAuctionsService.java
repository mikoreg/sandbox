package com.acme.auctions.core.auctioning.usecase;

import com.acme.auctions.core.auctioning.port.in.AuctionBrowsePage;
import com.acme.auctions.core.auctioning.port.in.BrowseAuctionsQuery;
import com.acme.auctions.core.auctioning.port.in.BrowseAuctionsUseCase;
import com.acme.auctions.core.auctioning.port.out.AuctionReadModel;
import com.acme.auctions.core.party.model.PartyId;

import java.util.Objects;

public final class BrowseAuctionsService implements BrowseAuctionsUseCase {

    private final AuctionReadModel auctionReadModel;

    public BrowseAuctionsService(AuctionReadModel auctionReadModel) {
        this.auctionReadModel = Objects.requireNonNull(auctionReadModel, "auctionReadModel must not be null");
    }

    @Override
    public AuctionBrowsePage browseAuctions(BrowseAuctionsQuery query) {
        return auctionReadModel.browseAuctions(query);
    }

    @Override
    public AuctionBrowsePage browseSellerAuctions(PartyId sellerId, BrowseAuctionsQuery query) {
        return auctionReadModel.browseSellerAuctions(sellerId, query);
    }

    @Override
    public AuctionBrowsePage browseBidderAuctions(PartyId bidderId, BrowseAuctionsQuery query) {
        return auctionReadModel.browseBidderAuctions(bidderId, query);
    }
}
