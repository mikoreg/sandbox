package com.acme.auctions.core.auctioning.model;

import java.util.Objects;
import java.util.UUID;

public record AuctionId(UUID value) {

    public AuctionId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static AuctionId newId() {
        return new AuctionId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
