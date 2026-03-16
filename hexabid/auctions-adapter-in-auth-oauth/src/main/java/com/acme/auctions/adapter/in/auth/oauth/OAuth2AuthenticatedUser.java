package com.acme.auctions.adapter.in.auth.oauth;

import com.acme.auctions.auth.core.identityaccess.model.AuthenticatedUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public record OAuth2AuthenticatedUser(AuthenticatedUser user) implements OAuth2User {

    @Override
    public Map<String, Object> getAttributes() {
        return Map.of(
                "partyId", user.partyId().value(),
                "provider", user.provider(),
                "subject", user.subject(),
                "displayName", user.displayName(),
                "email", user.email() != null ? user.email() : ""
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getName() {
        return user.displayName();
    }
}
