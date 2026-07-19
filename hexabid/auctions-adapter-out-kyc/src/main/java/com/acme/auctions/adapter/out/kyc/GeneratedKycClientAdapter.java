package com.acme.auctions.adapter.out.kyc;

import com.acme.auctions.core.auctioning.port.out.KycClient;
import com.acme.auctions.core.party.model.PartyId;
import com.acme.auctions.kyc.client.api.KycVerificationApi;
import com.acme.auctions.kyc.client.invoker.ApiException;
import org.springframework.stereotype.Component;

@Component
public class GeneratedKycClientAdapter implements KycClient {

    private final KycVerificationApi kycVerificationApi;

    public GeneratedKycClientAdapter(KycVerificationApi kycVerificationApi) {
        this.kycVerificationApi = kycVerificationApi;
    }

    @Override
    public boolean isVerified(PartyId partyId) {
        try {
            return kycVerificationApi.verifyActor(partyId.value()).getVerified();
        } catch (ApiException exception) {
            throw new IllegalStateException("KYC verification call failed", exception);
        }
    }
}
