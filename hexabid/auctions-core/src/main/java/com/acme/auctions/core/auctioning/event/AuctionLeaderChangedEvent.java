package com.acme.auctions.core.auctioning.event;

import com.acme.auctions.core.auctioning.model.AuctionId;
import com.acme.auctions.core.auctioning.model.Price;
import com.acme.auctions.core.party.model.PartyId;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.Objects;

public record AuctionLeaderChangedEvent(
        AuctionId auctionId,
        @Nullable PartyId previousLeaderId,
        PartyId newLeaderId,
        Price newLeadingPrice,
        Instant occurredAt
) implements AuctionDomainEvent {

    public AuctionLeaderChangedEvent {
        Objects.requireNonNull(auctionId, "auctionId must not be null");
        Objects.requireNonNull(newLeaderId, "newLeaderId must not be null");
        Objects.requireNonNull(newLeadingPrice, "newLeadingPrice must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
    }

    @Override
    public String type() {
        return "AUCTION_LEADER_CHANGED";
    }
}
