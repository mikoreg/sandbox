package com.acme.auctions.core.auctioning.port.in;

import com.acme.auctions.core.auctioning.model.AuctionStatus;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AuctionView(
        UUID auctionId,
        String sellerId,
        String title,
        String currency,
        String currentPrice,
        Instant endsAt,
        AuctionStatus status,
        @Nullable String leadingBidderId,
        List<BidView> biddingHistory
) {
}
