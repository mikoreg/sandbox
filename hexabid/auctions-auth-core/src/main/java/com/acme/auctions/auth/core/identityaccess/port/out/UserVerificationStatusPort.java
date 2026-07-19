package com.acme.auctions.auth.core.identityaccess.port.out;

import com.acme.auctions.core.party.model.PartyId;

public interface UserVerificationStatusPort {
    boolean isVerified(PartyId partyId);
}
