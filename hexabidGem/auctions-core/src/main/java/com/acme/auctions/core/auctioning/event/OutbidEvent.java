package main.java.com.acme.auctions.core.auctioning.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event published when a bid is outbid.
 */
public record OutbidEvent(UUID auctionId, UUID bidderId, Instant occurredAt) {
    public OutbidEvent {
        Objects.requireNonNull(auctionId, "Auction ID cannot be null");
        Objects.requireNonNull(bidderId, "Bidder ID cannot be null");
        Objects.requireNonNull(occurredAt, "Occurred at cannot be null");
    }
}