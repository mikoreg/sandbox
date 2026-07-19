package com.acme.auctions.auth.core.identityaccess.port.in;

import java.util.List;

public interface AuthenticationMechanismQuery {
    List<AuthenticationMechanism> getAvailableMechanisms();

    record AuthenticationMechanism(String id, String label, String loginUrl) {}
}
