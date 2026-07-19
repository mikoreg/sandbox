package com.acme.auctions.auth.core.identityaccess.model;

import com.acme.auctions.core.party.model.PartyId;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public record AuthenticatedUser(
        PartyId partyId,
        String provider,
        String subject,
        String displayName,
        @Nullable String email
) {

    public AuthenticatedUser {
        Objects.requireNonNull(partyId, "partyId must not be null");
        Objects.requireNonNull(provider, "provider must not be null");
        Objects.requireNonNull(subject, "subject must not be null");
        Objects.requireNonNull(displayName, "displayName must not be null");
    }
}
