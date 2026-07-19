package com.acme.auctions.core.auctioning.usecase;

import com.acme.auctions.core.auctioning.event.AuctionDomainEvent;
import com.acme.auctions.core.auctioning.event.AuctionLeaderChangedEvent;
import com.acme.auctions.core.auctioning.exception.AuctionConcurrencyConflictException;
import com.acme.auctions.core.auctioning.model.Auction;
import com.acme.auctions.core.auctioning.model.AuctionId;
import com.acme.auctions.core.auctioning.model.Price;
import com.acme.auctions.core.auctioning.port.in.PlaceBidCommand;
import com.acme.auctions.core.auctioning.port.in.PlaceBidFailureReason;
import com.acme.auctions.core.auctioning.port.in.PlaceBidResult;
import com.acme.auctions.core.auctioning.port.out.AuctionEventPublisher;
import com.acme.auctions.core.auctioning.port.out.AuctionRepository;
import com.acme.auctions.core.auctioning.port.out.KycClient;
import com.acme.auctions.core.lot.model.Lot;
import com.acme.auctions.core.party.model.PartyId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlaceBidServiceTest {

    @Test
    void shouldPublishLeaderChangedEventWhenLeaderChanges() {
        Instant now = Instant.parse("2026-03-05T12:00:00Z");
        Clock clock = Clock.fixed(now, ZoneOffset.UTC);
        InMemoryAuctionRepository repository = new InMemoryAuctionRepository();
        RecordingAuctionEventPublisher events = new RecordingAuctionEventPublisher();
        KycClient kycClient = partyId -> true;

        Auction auction = Auction.create(
                AuctionId.newId(),
                new PartyId("seller-1"),
                Lot.singleProductDraft("Rare comic book"),
                new Price(new BigDecimal("100.00"), "PLN"),
                now.plusSeconds(3600)
        );
        auction.placeBid(new PartyId("bidder-1"), new Price(new BigDecimal("120.00"), "PLN"), now.minusSeconds(60));
        repository.save(auction);

        PlaceBidService service = new PlaceBidService(repository, kycClient, events, clock);

        PlaceBidResult result = service.placeBid(new PlaceBidCommand(
                auction.id(),
                new PartyId("bidder-2"),
                new Price(new BigDecimal("130.00"), "PLN")
        ));
        PlaceBidResult.BidAccepted accepted = assertInstanceOf(PlaceBidResult.BidAccepted.class, result);
        var placedBid = accepted.bid();

        assertEquals("130.00", placedBid.amount());
        assertEquals(1, events.events.size());
        assertInstanceOf(AuctionLeaderChangedEvent.class, events.events.getFirst());
    }

    @Test
    void shouldRejectBidderWithoutKyc() {
        Instant now = Instant.parse("2026-03-05T12:00:00Z");
        Clock clock = Clock.fixed(now, ZoneOffset.UTC);
        InMemoryAuctionRepository repository = new InMemoryAuctionRepository();
        RecordingAuctionEventPublisher events = new RecordingAuctionEventPublisher();
        KycClient kycClient = partyId -> false;

        Auction auction = Auction.create(
                AuctionId.newId(),
                new PartyId("seller-1"),
                Lot.singleProductDraft("Collector camera"),
                new Price(new BigDecimal("400.00"), "PLN"),
                now.plusSeconds(3600)
        );
        repository.save(auction);

        PlaceBidService service = new PlaceBidService(repository, kycClient, events, clock);

        PlaceBidResult result = service.placeBid(new PlaceBidCommand(
                auction.id(),
                new PartyId("bidder-2"),
                new Price(new BigDecimal("500.00"), "PLN")
        ));
        PlaceBidResult.BidRejected rejected = assertInstanceOf(PlaceBidResult.BidRejected.class, result);

        assertEquals(PlaceBidFailureReason.BIDDER_NOT_VERIFIED, rejected.reason());
        assertTrue(rejected.message().contains("KYC"));
    }

    @Test
    void shouldReturnConflictResultWhenAuctionWasModifiedConcurrently() {
        Instant now = Instant.parse("2026-03-05T12:00:00Z");
        Clock clock = Clock.fixed(now, ZoneOffset.UTC);
        InMemoryAuctionRepository repository = new InMemoryAuctionRepository();
        RecordingAuctionEventPublisher events = new RecordingAuctionEventPublisher();
        KycClient kycClient = partyId -> true;

        Auction auction = Auction.create(
                AuctionId.newId(),
                new PartyId("seller-1"),
                Lot.singleProductDraft("Console"),
                new Price(new BigDecimal("1000.00"), "PLN"),
                now.plusSeconds(3600)
        );
        repository.save(auction);
        repository.failOnSave = true;

        PlaceBidService service = new PlaceBidService(repository, kycClient, events, clock);

        PlaceBidResult result = service.placeBid(new PlaceBidCommand(
                auction.id(),
                new PartyId("bidder-2"),
                new Price(new BigDecimal("1100.00"), "PLN")
        ));
        PlaceBidResult.BidRejected rejected = assertInstanceOf(PlaceBidResult.BidRejected.class, result);

        assertEquals(PlaceBidFailureReason.CONCURRENT_MODIFICATION, rejected.reason());
        assertTrue(rejected.message().contains("concurrently"));
    }

    private static final class InMemoryAuctionRepository implements AuctionRepository {

        private final Map<AuctionId, Auction> storage = new HashMap<>();
        private boolean failOnSave;

        @Override
        public Auction save(Auction auction) {
            if (failOnSave) {
                throw new AuctionConcurrencyConflictException(auction.id());
            }
            storage.put(auction.id(), auction);
            return auction;
        }

        @Override
        public Optional<Auction> findById(AuctionId auctionId) {
            return Optional.ofNullable(storage.get(auctionId));
        }

        @Override
        public List<Auction> findExpiredOpenAuctions(Instant currentTime) {
            return storage.values().stream()
                    .filter(auction -> auction.status().name().equals("OPEN"))
                    .filter(auction -> auction.isExpiredAt(currentTime))
                    .toList();
        }
    }

    private static final class RecordingAuctionEventPublisher implements AuctionEventPublisher {

        private final List<AuctionDomainEvent> events = new ArrayList<>();

        @Override
        public void publish(AuctionDomainEvent event) {
            events.add(event);
        }
    }
}
