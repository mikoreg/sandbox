package com.acme.auctions.adapter.in.auth.local;

import com.acme.auctions.auth.core.identityaccess.port.out.AuthProviderDescriptor;
import com.acme.auctions.auth.core.identityaccess.port.out.AuthProviderDiscoverer;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LocalAuthProviderDiscoverer implements AuthProviderDiscoverer {

    private final LocalAuthProviderDescriptor descriptor;

    public LocalAuthProviderDiscoverer(LocalAuthProviderDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public List<AuthProviderDescriptor> getProviders() {
        return List.of(descriptor);
    }
}
