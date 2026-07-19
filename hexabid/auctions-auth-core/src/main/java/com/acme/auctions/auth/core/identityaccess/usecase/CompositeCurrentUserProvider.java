package com.acme.auctions.auth.core.identityaccess.usecase;

import com.acme.auctions.auth.core.identityaccess.model.AuthenticatedUser;
import com.acme.auctions.auth.core.identityaccess.port.out.CurrentUserProvider;

import java.util.List;
import java.util.Optional;

/**
 * Kompozyt dostawców tożsamości (wzorzec LISTY).
 *
 * Iteruje po wszystkich zarejestrowanych dostawcach uwierzytelniania
 * i zwraca pierwszego rozpoznanego użytkownika. Pozwala na współistnienie
 * wielu mechanizmów uwierzytelniania (np. OAuth2 + Local).
 */
public class CompositeCurrentUserProvider implements CurrentUserProvider {

    private final List<CurrentUserProvider> providers;

    public CompositeCurrentUserProvider(List<CurrentUserProvider> providers) {
        this.providers = List.copyOf(providers);
    }

    @Override
    public Optional<AuthenticatedUser> maybeCurrentUser() {
        for (CurrentUserProvider provider : providers) {
            Optional<AuthenticatedUser> user = provider.maybeCurrentUser();
            if (user.isPresent()) {
                return user;
            }
        }
        return Optional.empty();
    }
}
