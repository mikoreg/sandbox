package com.acme.auctions.core.auctioning.port.in;

import com.acme.auctions.core.auctioning.model.AuctionStatus;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

public record AuctionSummaryView(
        UUID auctionId,
        String sellerId,
        String title,
        String currentPrice,
        String currency,
        Instant endsAt,
        AuctionStatus status,
        @Nullable String leadingBidderId
) {
}
