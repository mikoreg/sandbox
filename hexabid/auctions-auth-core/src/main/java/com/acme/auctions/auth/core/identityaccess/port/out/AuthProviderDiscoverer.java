package com.acme.auctions.auth.core.identityaccess.port.out;

import java.util.List;

/**
 * Port pozwalający na odkrywanie dostępnych metod uwierzytelniania.
 */
public interface AuthProviderDiscoverer {
    List<AuthProviderDescriptor> getProviders();
}
