package com.acme.auctions.core.auctioning.port.out;

import com.acme.auctions.core.party.model.PartyId;

public interface KycClient {
    boolean isVerified(PartyId partyId);
}
