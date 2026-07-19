package com.acme.auctions.core.auctioning.usecase;

import com.acme.auctions.core.auctioning.model.Auction;
import com.acme.auctions.core.auctioning.model.Bid;
import com.acme.auctions.core.auctioning.port.in.AuctionView;
import com.acme.auctions.core.auctioning.port.in.BidView;

import java.util.List;

final class AuctionViews {

    private AuctionViews() {
    }

    static AuctionView from(Auction auction) {
        String leader = auction.maybeWinnerId().map(Object::toString).orElse(null);
        return new AuctionView(
                auction.id().value(),
                auction.sellerId().value(),
                auction.title(),
                auction.currentPrice().currency(),
                auction.currentPrice().amount().toPlainString(),
                auction.endsAt(),
                auction.status(),
                leader,
                auction.biddingHistory().stream().map(AuctionViews::from).toList()
        );
    }

    static BidView from(Bid bid) {
        return new BidView(
                bid.bidderId().value(),
                bid.amount().amount().toPlainString(),
                bid.amount().currency(),
                bid.placedAt()
        );
    }
}
