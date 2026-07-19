package com.acme.auctions.auth.core.identityaccess.usecase;

import com.acme.auctions.auth.core.identityaccess.model.AuthenticatedUser;
import com.acme.auctions.auth.core.identityaccess.port.in.CurrentUserProfileView;
import com.acme.auctions.auth.core.identityaccess.port.in.FindCurrentUserProfileUseCase;
import com.acme.auctions.auth.core.identityaccess.port.out.CurrentUserProvider;
import com.acme.auctions.auth.core.identityaccess.port.out.UserVerificationStatusPort;

import java.util.Objects;
import java.util.Optional;

public final class FindCurrentUserProfileService implements FindCurrentUserProfileUseCase {

    private final CurrentUserProvider currentUserProvider;
    private final UserVerificationStatusPort userVerificationStatusPort;

    public FindCurrentUserProfileService(
            CurrentUserProvider currentUserProvider,
            UserVerificationStatusPort userVerificationStatusPort
    ) {
        this.currentUserProvider = Objects.requireNonNull(currentUserProvider, "currentUserProvider must not be null");
        this.userVerificationStatusPort = Objects.requireNonNull(userVerificationStatusPort, "userVerificationStatusPort must not be null");
    }

    @Override
    public Optional<CurrentUserProfileView> findCurrentUserProfile() {
        return currentUserProvider.maybeCurrentUser()
                .map(this::toView);
    }

    private CurrentUserProfileView toView(AuthenticatedUser user) {
        return new CurrentUserProfileView(
                user.partyId().value(),
                user.provider(),
                user.displayName(),
                user.email(),
                userVerificationStatusPort.isVerified(user.partyId())
        );
    }
}
