package com.acme.auctions.adapter.in.auth.oauth.dev;

import com.acme.auctions.adapter.in.auth.oauth.OAuth2AuthenticatedUser;
import com.acme.auctions.auth.core.identityaccess.model.AuthenticatedUser;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

public class DevOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final DevUserCatalog devUserCatalog;

    public DevOAuth2UserService(DevUserCatalog devUserCatalog) {
        this.devUserCatalog = devUserCatalog;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        final String username = Optional.ofNullable(userRequest.getAdditionalParameters().get("username"))
                .map(Object::toString)
                .orElseThrow(() -> new OAuth2AuthenticationException("Missing username parameter"));

        return devUserCatalog.findByUsername(username)
                .map(DevUserCatalog.DevUserEntry::toAuthenticatedUser)
                .map(OAuth2AuthenticatedUser::new)
                .orElseThrow(() -> new OAuth2AuthenticationException("Unknown dev user: " + username));
    }
}
