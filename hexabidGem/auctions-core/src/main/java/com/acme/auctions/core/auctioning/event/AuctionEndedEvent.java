package com.acme.auctions.core.auctioning.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event published when an auction ends.
 */
public record AuctionEndedEvent(UUID auctionId, Instant endedAt) {
    public AuctionEndedEvent {
        Objects.requireNonNull(auctionId, "Auction ID cannot be null");
        Objects.requireNonNull(endedAt, "Ended at cannot be null");
    }
}