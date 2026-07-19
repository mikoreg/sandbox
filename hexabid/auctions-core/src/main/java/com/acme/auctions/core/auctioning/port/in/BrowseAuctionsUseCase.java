package com.acme.auctions.core.auctioning.port.in;

import com.acme.auctions.core.party.model.PartyId;

public interface BrowseAuctionsUseCase {
    AuctionBrowsePage browseAuctions(BrowseAuctionsQuery query);

    AuctionBrowsePage browseSellerAuctions(PartyId sellerId, BrowseAuctionsQuery query);

    AuctionBrowsePage browseBidderAuctions(PartyId bidderId, BrowseAuctionsQuery query);
}
