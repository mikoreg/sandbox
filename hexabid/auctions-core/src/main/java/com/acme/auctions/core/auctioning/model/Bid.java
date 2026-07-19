package com.acme.auctions.core.auctioning.model;

import com.acme.auctions.core.party.model.PartyId;

import java.time.Instant;
import java.util.Objects;

public record Bid(PartyId bidderId, Price amount, Instant placedAt) {

    public Bid {
        Objects.requireNonNull(bidderId, "bidderId must not be null");
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(placedAt, "placedAt must not be null");
    }
}
