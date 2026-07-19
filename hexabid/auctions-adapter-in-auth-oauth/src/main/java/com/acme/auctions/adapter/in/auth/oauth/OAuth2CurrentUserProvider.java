package com.acme.auctions.adapter.in.auth.oauth;

import com.acme.auctions.auth.core.identityaccess.model.AuthenticatedUser;
import com.acme.auctions.auth.core.identityaccess.port.out.CurrentUserProvider;
import com.acme.auctions.core.party.model.PartyId;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OAuth2CurrentUserProvider implements CurrentUserProvider {

    @Override
    public Optional<AuthenticatedUser> maybeCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthenticatedUser authenticatedUser) {
            return Optional.of(authenticatedUser);
        }
        if (principal instanceof OAuth2AuthenticatedUser wrapper) {
            return Optional.of(wrapper.user());
        }
        if (!(authentication instanceof OAuth2AuthenticationToken oauth2AuthenticationToken)) {
            return Optional.empty();
        }
        if (!(principal instanceof OAuth2User oauth2User)) {
            return Optional.of(new AuthenticatedUser(new PartyId("unknown"), "unknown", "unknown", "Unknown", null));
        }
        String provider = oauth2AuthenticationToken.getAuthorizedClientRegistrationId();
        String subject = subjectOf(oauth2User);
        PartyId partyId = new PartyId(provider + ":" + subject);
        String displayName = displayNameOf(oauth2User);
        @Nullable String email = oauth2User.getAttribute("email");
        return Optional.of(new AuthenticatedUser(partyId, provider, subject, displayName, email));
    }

    private static String subjectOf(OAuth2User oauth2User) {
        if (oauth2User instanceof OidcUser oidcUser) {
            return oidcUser.getSubject();
        }
        Object id = oauth2User.getAttributes().get("id");
        return id == null ? oauth2User.getName() : id.toString();
    }

    private static String displayNameOf(OAuth2User oauth2User) {
        Object name = oauth2User.getAttributes().get("name");
        if (name != null) {
            return name.toString();
        }
        Object login = oauth2User.getAttributes().get("login");
        if (login != null) {
            return login.toString();
        }
        return oauth2User.getName();
    }
}
