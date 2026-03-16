package com.acme.auctions.adapter.in.auth.oauth.dev;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
@Profile("dev")
class DevAuthConfiguration {

    @Bean
    DevOAuth2UserService devOauth2UserService(final DevUserCatalog devUserCatalog) {
        return new DevOAuth2UserService(devUserCatalog);
    }

    @Bean
    DevUserCatalog devUserCatalog() {
        return new DevUserCatalog(List.of(
                new DevUserCatalog.DevUserEntry(
                        "seller-anna",
                        "Anna Developer",
                        "anna.dev@hexabid.local",
                        true,
                        "Sprzedajacy z aktywnymi aukcjami nieruchomosci i mienia."
                ),
                new DevUserCatalog.DevUserEntry(
                        "seller-marek",
                        "Marek Demo",
                        "marek.dev@hexabid.local",
                        true,
                        "Sprzedajacy z aukcjami motoryzacyjnymi i kolekcjonerskimi."
                ),
                new DevUserCatalog.DevUserEntry(
                        "bidder-ola",
                        "Ola Bidder",
                        "ola.dev@hexabid.local",
                        true,
                        "Aktywny kupujacy, idealny do testow licytacji."
                ),
                new DevUserCatalog.DevUserEntry(
                        "bidder-jan",
                        "Jan Buyer",
                        "jan.dev@hexabid.local",
                        true,
                        "Kupujacy bioracy udzial w wielu aukcjach."
                ),
                new DevUserCatalog.DevUserEntry(
                        "bidder-lena",
                        "Lena Watcher",
                        "lena.dev@hexabid.local",
                        false,
                        "Uzytkownik bez weryfikacji KYC do scenariuszy negatywnych."
                )
        ));
    }
}
