package com.acme.auctions.core.party.model;

import java.util.Objects;

public record PartyId(String value) {

    public PartyId {
        Objects.requireNonNull(value, "value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
