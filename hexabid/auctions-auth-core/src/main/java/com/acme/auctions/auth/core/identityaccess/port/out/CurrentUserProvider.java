package com.acme.auctions.auth.core.identityaccess.port.out;

import com.acme.auctions.auth.core.identityaccess.model.AuthenticatedUser;

import java.util.Optional;

public interface CurrentUserProvider {
    Optional<AuthenticatedUser> maybeCurrentUser();
}
