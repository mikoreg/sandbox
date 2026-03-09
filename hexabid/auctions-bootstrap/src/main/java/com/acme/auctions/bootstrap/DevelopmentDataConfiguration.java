package com.acme.auctions.bootstrap;

import com.acme.auctions.core.auctioning.model.Auction;
import com.acme.auctions.core.auctioning.model.AuctionId;
import com.acme.auctions.core.auctioning.model.Price;
import com.acme.auctions.core.auctioning.port.in.AuctionSort;
import com.acme.auctions.core.auctioning.port.in.BrowseAuctionsQuery;
import com.acme.auctions.core.auctioning.port.out.AuctionReadModel;
import com.acme.auctions.core.auctioning.port.out.AuctionRepository;
import com.acme.auctions.core.lot.model.Lot;
import com.acme.auctions.core.party.model.PartyId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Configuration
@Profile("dev")
class DevelopmentDataConfiguration {

    @Bean
    ApplicationRunner developmentDataInitializer(
            AuctionReadModel auctionReadModel,
            AuctionRepository auctionRepository,
            Clock clock,
            @Value("${auctions.seed.enabled:true}") boolean seedEnabled
    ) {
        return args -> {
            if (!seedEnabled || hasAnyAuction(auctionReadModel)) {
                return;
            }

            Instant now = clock.instant();
            List<Auction> sampleAuctions = List.of(
                    activeAuction(
                            "Apartament z tarasem w Gdyni",
                            "google:seller-anna",
                            money("485000.00", "PLN"),
                            now.plus(Duration.ofDays(6)),
                            List.of(new BidSeed("github:bidder-ola", "501000.00", now.minus(Duration.ofHours(6))))
                    ),
                    activeAuction(
                            "Ford Mustang Fastback 1968",
                            "github:seller-marek",
                            money("185000.00", "PLN"),
                            now.plus(Duration.ofDays(4)),
                            List.of(
                                    new BidSeed("google:bidder-jan", "192500.00", now.minus(Duration.ofHours(8))),
                                    new BidSeed("github:bidder-ola", "198000.00", now.minus(Duration.ofHours(2)))
                            )
                    ),
                    activeAuction(
                            "Kolekcja komiksow z lat 90.",
                            "google:seller-julia",
                            money("1200.00", "PLN"),
                            now.plus(Duration.ofDays(2)),
                            List.of(new BidSeed("github:bidder-piotr", "1480.00", now.minus(Duration.ofMinutes(40))))
                    ),
                    activeAuction(
                            "Maszyna CNC po przegladzie serwisowym",
                            "github:seller-adam",
                            money("27500.00", "PLN"),
                            now.plus(Duration.ofDays(9)),
                            List.of()
                    ),
                    activeAuction(
                            "Fotografia kolekcjonerska - edycja limitowana",
                            "google:seller-kasia",
                            money("950.00", "PLN"),
                            now.plus(Duration.ofHours(19)),
                            List.of(
                                    new BidSeed("github:bidder-lena", "1100.00", now.minus(Duration.ofHours(5))),
                                    new BidSeed("google:bidder-michal", "1250.00", now.minus(Duration.ofMinutes(55)))
                            )
                    ),
                    activeAuction(
                            "Zestaw mebli loftowych do biura",
                            "github:seller-robert",
                            money("8900.00", "PLN"),
                            now.plus(Duration.ofDays(3)),
                            List.of(new BidSeed("google:bidder-ola", "9300.00", now.minus(Duration.ofHours(4))))
                    ),
                    activeAuction(
                            "Skuter Vespa Primavera",
                            "google:seller-ewa",
                            money("3200.00", "EUR"),
                            now.plus(Duration.ofDays(5)),
                            List.of(
                                    new BidSeed("github:bidder-nina", "3400.00", now.minus(Duration.ofHours(10))),
                                    new BidSeed("google:bidder-tomek", "3600.00", now.minus(Duration.ofHours(3)))
                            )
                    ),
                    activeAuction(
                            "Gitara Fender Stratocaster USA",
                            "github:seller-pawel",
                            money("7800.00", "PLN"),
                            now.plus(Duration.ofDays(1)),
                            List.of(new BidSeed("google:bidder-jan", "8200.00", now.minus(Duration.ofMinutes(25))))
                    ),
                    closedAuction(
                            "Dzialka rekreacyjna nad jeziorem",
                            "google:seller-anna",
                            money("99000.00", "PLN"),
                            now.minus(Duration.ofDays(2)),
                            List.of(
                                    new BidSeed("github:bidder-ola", "103000.00", now.minus(Duration.ofDays(3))),
                                    new BidSeed("google:bidder-tomek", "108500.00", now.minus(Duration.ofDays(2)).minus(Duration.ofHours(2)))
                            ),
                            now
                    ),
                    closedAuction(
                            "Konsola retro z kolekcja gier",
                            "github:seller-marek",
                            money("650.00", "PLN"),
                            now.minus(Duration.ofDays(1)),
                            List.of(),
                            now
                    )
            );

            sampleAuctions.forEach(auctionRepository::save);
        };
    }

    private static boolean hasAnyAuction(AuctionReadModel auctionReadModel) {
        return !auctionReadModel
                .browseAuctions(new BrowseAuctionsQuery(null, null, AuctionSort.ENDING_SOON, 1, null))
                .items()
                .isEmpty();
    }

    private static Auction activeAuction(
            String title,
            String sellerId,
            Price startingPrice,
            Instant endsAt,
            List<BidSeed> bids
    ) {
        Auction auction = Auction.create(
                AuctionId.newId(),
                new PartyId(sellerId),
                Lot.singleProductDraft(title),
                startingPrice,
                endsAt
        );
        bids.forEach(bid -> auction.placeBid(new PartyId(bid.bidderId()), new Price(new BigDecimal(bid.amount()), startingPrice.currency()), bid.placedAt()));
        return auction;
    }

    private static Auction closedAuction(
            String title,
            String sellerId,
            Price startingPrice,
            Instant endsAt,
            List<BidSeed> bids,
            Instant now
    ) {
        Auction auction = activeAuction(title, sellerId, startingPrice, endsAt, bids);
        auction.maybeCloseIfExpired(now);
        return auction;
    }

    private static Price money(String amount, String currency) {
        return new Price(new BigDecimal(amount), currency);
    }

    private record BidSeed(String bidderId, String amount, Instant placedAt) {
    }
}
