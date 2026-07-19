package com.acme.auctions.auth.core.identityaccess.port.out;

/**
 * Deskryptor dostawcy uwierzytelniania.
 * Reprezentuje dostawcę zgodnego ze standardem federacyjnym (np. OAuth2/OIDC).
 */
public interface AuthProviderDescriptor {
    String getRegistrationId();
    String getName();
    String getLoginUrl();
}
