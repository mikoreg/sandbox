package com.acme.auctions.bootstrap;

import com.acme.auctions.adapter.in.auth.oauth.dev.DevUserCatalog;
import com.acme.auctions.adapter.out.kyc.GeneratedKycClientAdapter;
import com.acme.auctions.core.auctioning.port.out.KycClient;
import com.acme.auctions.core.party.model.PartyId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
class DevelopmentSecurityConfiguration {

    @Bean
    @Primary
    KycClient developmentKycClient(GeneratedKycClientAdapter delegate, DevUserCatalog devUserCatalog) {
        return partyId -> isDevVerifiedParty(partyId, devUserCatalog) || delegate.isVerified(partyId);
    }

    private static boolean isDevVerifiedParty(PartyId partyId, DevUserCatalog devUserCatalog) {
        return devUserCatalog.users().stream()
                .anyMatch(user -> user.verified() && ("dev:" + user.username()).equals(partyId.value()));
    }
}
