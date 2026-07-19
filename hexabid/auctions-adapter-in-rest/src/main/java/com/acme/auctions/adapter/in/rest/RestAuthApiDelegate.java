package com.acme.auctions.adapter.in.rest;

import com.acme.auctions.auth.core.identityaccess.port.out.AuthProviderDescriptor;
import com.acme.auctions.auth.core.identityaccess.port.out.AuthProviderDiscoverer;
import com.acme.auctions.contract.auth.api.AuthApiDelegate;
import com.acme.auctions.contract.auth.model.AuthProviderResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@org.slf4j.extern.slf4j.Slf4j
public class RestAuthApiDelegate implements AuthApiDelegate {

    private final List<AuthProviderDiscoverer> discoverers;

    public RestAuthApiDelegate(List<AuthProviderDiscoverer> discoverers) {
        this.discoverers = discoverers;
        log.info("Initialized RestAuthApiDelegate with {} discoverers", discoverers.size());
    }

    @Override
    public ResponseEntity<List<AuthProviderResponse>> getAuthProviders() {
        log.debug("Fetching auth providers from {} discoverers", discoverers.size());
        List<AuthProviderResponse> providers = discoverers.stream()
                .flatMap(d -> {
                    List<AuthProviderDescriptor> p = d.getProviders();
                    log.debug("Discoverer {} provided {} providers", d.getClass().getSimpleName(), p.size());
                    return p.stream();
                })
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        log.info("Returning {} auth providers", providers.size());
        return ResponseEntity.ok(providers);
    }

    private AuthProviderResponse mapToResponse(AuthProviderDescriptor descriptor) {
        AuthProviderResponse response = new AuthProviderResponse();
        response.setRegistrationId(descriptor.getRegistrationId());
        response.setName(descriptor.getName());
        response.setLoginUrl(descriptor.getLoginUrl());
        return response;
    }
}
