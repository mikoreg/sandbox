package com.acme.auctions.core.party.model;

import java.util.Objects;
import java.util.Set;

public record Party(PartyId partyId, String displayName, Set<PartyRole> roles) {

    public Party {
        Objects.requireNonNull(partyId, "partyId must not be null");
        Objects.requireNonNull(displayName, "displayName must not be null");
        Objects.requireNonNull(roles, "roles must not be null");
        if (displayName.isBlank()) {
            throw new IllegalArgumentException("displayName must not be blank");
        }
        roles = Set.copyOf(roles);
    }
}
