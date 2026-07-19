package com.acme.auctions.core.auctioning.event;

import com.acme.auctions.core.auctioning.model.AuctionId;
import com.acme.auctions.core.auctioning.model.Price;
import com.acme.auctions.core.party.model.PartyId;

import java.time.Instant;
import java.util.Objects;

public record AuctionWonEvent(
        AuctionId auctionId,
        PartyId winnerId,
        Price winningPrice,
        Instant occurredAt
) implements AuctionDomainEvent {

    public AuctionWonEvent {
        Objects.requireNonNull(auctionId, "auctionId must not be null");
        Objects.requireNonNull(winnerId, "winnerId must not be null");
        Objects.requireNonNull(winningPrice, "winningPrice must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
    }

    @Override
    public String type() {
        return "AUCTION_WON";
    }
}
