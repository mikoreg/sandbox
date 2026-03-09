package com.acme.auctions.core.auctioning.usecase;

import com.acme.auctions.core.auctioning.model.Auction;
import com.acme.auctions.core.auctioning.model.AuctionId;
import com.acme.auctions.core.auctioning.model.Price;
import com.acme.auctions.core.auctioning.port.in.CreateAuctionCommand;
import com.acme.auctions.core.auctioning.port.in.CreateAuctionFailureReason;
import com.acme.auctions.core.auctioning.port.in.CreateAuctionResult;
import com.acme.auctions.core.auctioning.port.out.AuctionRepository;
import com.acme.auctions.core.auctioning.port.out.KycClient;
import com.acme.auctions.core.party.model.PartyId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class CreateAuctionServiceTest {

    @Test
    void shouldRejectAuctionCreationWhenSellerDidNotPassKyc() {
        Instant now = Instant.parse("2026-03-05T12:00:00Z");
        Clock clock = Clock.fixed(now, ZoneOffset.UTC);
        InMemoryAuctionRepository repository = new InMemoryAuctionRepository();
        KycClient kycClient = partyId -> false;
        CreateAuctionService service = new CreateAuctionService(repository, kycClient, clock);

        CreateAuctionResult result = service.createAuction(new CreateAuctionCommand(
                new PartyId("seller-1"),
                "Painting",
                new Price(new BigDecimal("300.00"), "PLN"),
                now.plusSeconds(3600)
        ));
        CreateAuctionResult.AuctionCreationRejected rejected = assertInstanceOf(CreateAuctionResult.AuctionCreationRejected.class, result);

        assertEquals(CreateAuctionFailureReason.SELLER_NOT_VERIFIED, rejected.reason());
    }

    @Test
    void shouldCreateAuctionWhenInputIsValid() {
        Instant now = Instant.parse("2026-03-05T12:00:00Z");
        Clock clock = Clock.fixed(now, ZoneOffset.UTC);
        InMemoryAuctionRepository repository = new InMemoryAuctionRepository();
        KycClient kycClient = partyId -> true;
        CreateAuctionService service = new CreateAuctionService(repository, kycClient, clock);

        CreateAuctionResult result = service.createAuction(new CreateAuctionCommand(
                new PartyId("seller-1"),
                "Painting",
                new Price(new BigDecimal("300.00"), "PLN"),
                now.plusSeconds(3600)
        ));
        CreateAuctionResult.AuctionCreated created = assertInstanceOf(CreateAuctionResult.AuctionCreated.class, result);

        assertEquals("seller-1", created.auction().sellerId());
        assertEquals(1, repository.storage.size());
    }

    private static final class InMemoryAuctionRepository implements AuctionRepository {

        private final Map<AuctionId, Auction> storage = new HashMap<>();

        @Override
        public Auction save(Auction auction) {
            storage.put(auction.id(), auction);
            return auction;
        }

        @Override
        public Optional<Auction> findById(AuctionId auctionId) {
            return Optional.ofNullable(storage.get(auctionId));
        }

        @Override
        public List<Auction> findExpiredOpenAuctions(Instant currentTime) {
            return List.of();
        }
    }
}
