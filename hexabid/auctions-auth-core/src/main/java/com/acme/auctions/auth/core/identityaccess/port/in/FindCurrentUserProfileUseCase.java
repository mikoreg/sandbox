package com.acme.auctions.auth.core.identityaccess.port.in;

import java.util.Optional;

public interface FindCurrentUserProfileUseCase {
    Optional<CurrentUserProfileView> findCurrentUserProfile();
}
