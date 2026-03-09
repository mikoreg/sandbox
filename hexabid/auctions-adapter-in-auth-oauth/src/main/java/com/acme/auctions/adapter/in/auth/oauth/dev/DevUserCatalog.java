package com.acme.auctions.adapter.in.auth.oauth.dev;

import com.acme.auctions.auth.core.identityaccess.model.AuthenticatedUser;
import com.acme.auctions.core.party.model.PartyId;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DevUserCatalog {

    private final List<DevUserEntry> users;

    public DevUserCatalog(List<DevUserEntry> users) {
        this.users = List.copyOf(users);
    }

    public List<DevUserEntry> users() {
        return users;
    }

    public Optional<DevUserEntry> findByUsername(String username) {
        return users.stream()
                .filter(user -> user.username().equals(username))
                .findFirst();
    }

    public record DevUserEntry(
            String username,
            String displayName,
            String email,
            boolean verified,
            String description
    ) {

        public DevUserEntry {
            Objects.requireNonNull(username, "username must not be null");
            Objects.requireNonNull(displayName, "displayName must not be null");
            Objects.requireNonNull(email, "email must not be null");
            Objects.requireNonNull(description, "description must not be null");
        }

        public AuthenticatedUser toAuthenticatedUser() {
            return new AuthenticatedUser(
                    new PartyId("dev:" + username),
                    "dev",
                    username,
                    displayName,
                    email
            );
        }
    }
}
