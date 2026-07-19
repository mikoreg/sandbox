package com.acme.auctions.core.auctioning.port.out;

import com.acme.auctions.core.auctioning.port.in.AuctionBrowsePage;
import com.acme.auctions.core.auctioning.port.in.BrowseAuctionsQuery;
import com.acme.auctions.core.party.model.PartyId;

public interface AuctionReadModel {
    AuctionBrowsePage browseAuctions(BrowseAuctionsQuery query);

    AuctionBrowsePage browseSellerAuctions(PartyId sellerId, BrowseAuctionsQuery query);

    AuctionBrowsePage browseBidderAuctions(PartyId bidderId, BrowseAuctionsQuery query);
}
