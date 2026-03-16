package com.acme.auctions.adapter.in.auth.oauth;

import com.acme.auctions.auth.core.identityaccess.port.out.AuthProviderDescriptor;
import com.acme.auctions.auth.core.identityaccess.port.out.AuthProviderDiscoverer;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class OAuth2AuthProviderDiscoverer implements AuthProviderDiscoverer {

    private final ClientRegistrationRepository clientRegistrationRepository;

    public OAuth2AuthProviderDiscoverer(ClientRegistrationRepository clientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    @Override
    public List<AuthProviderDescriptor> getProviders() {
        if (clientRegistrationRepository instanceof Iterable) {
            return StreamSupport.stream(((Iterable<ClientRegistration>) clientRegistrationRepository).spliterator(), false)
                    .map(OAuth2ProviderDescriptor::new)
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    private static class OAuth2ProviderDescriptor implements AuthProviderDescriptor {
        private final ClientRegistration registration;

        public OAuth2ProviderDescriptor(ClientRegistration registration) {
            this.registration = registration;
        }

        @Override
        public String getRegistrationId() {
            return registration.getRegistrationId();
        }

        @Override
        public String getName() {
            return registration.getClientName();
        }

        @Override
        public String getLoginUrl() {
            return "/oauth2/authorization/" + registration.getRegistrationId();
        }
    }
}
