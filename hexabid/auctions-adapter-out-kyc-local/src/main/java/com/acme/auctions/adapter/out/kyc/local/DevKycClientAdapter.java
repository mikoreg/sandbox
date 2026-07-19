package com.acme.auctions.adapter.out.kyc.local;

import com.acme.auctions.core.auctioning.port.out.KycClient;
import com.acme.auctions.core.party.model.PartyId;
import org.springframework.stereotype.Component;

@Component
public class DevKycClientAdapter implements KycClient {

    @Override
    public boolean isVerified(PartyId partyId) {
        String idVal = partyId.value();
        if ("dev:bidder-lena".equals(idVal)) {
            return false;
        }
        return idVal.startsWith("dev:") || idVal.startsWith("local:");
    }
}
