package com.acme.auctions.core.auctioning.usecase;

import com.acme.auctions.core.auctioning.event.AuctionDomainEvent;
import com.acme.auctions.core.auctioning.event.AuctionClosedWithoutWinnerEvent;
import com.acme.auctions.core.auctioning.exception.AuctionConcurrencyConflictException;
import com.acme.auctions.core.auctioning.model.Auction;
import com.acme.auctions.core.auctioning.model.AuctionId;
import com.acme.auctions.core.auctioning.model.Price;
import com.acme.auctions.core.auctioning.port.in.CloseExpiredAuctionsCommand;
import com.acme.auctions.core.auctioning.port.in.ClosedAuctionsResult;
import com.acme.auctions.core.auctioning.port.out.AuctionEventPublisher;
import com.acme.auctions.core.auctioning.port.out.AuctionRepository;
import com.acme.auctions.core.lot.model.Lot;
import com.acme.auctions.core.party.model.PartyId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class CloseExpiredAuctionsServiceTest {

    @Test
    void shouldCloseOnlyExpiredOpenAuctions() {
        Instant now = Instant.parse("2026-03-05T12:00:00Z");
        InMemoryAuctionRepository repository = new InMemoryAuctionRepository();
        RecordingAuctionEventPublisher events = new RecordingAuctionEventPublisher();

        Auction expired = Auction.create(
                AuctionId.newId(),
                new PartyId("seller-1"),
                Lot.singleProductDraft("Vinyl"),
                new Price(new BigDecimal("50.00"), "PLN"),
                now.minusSeconds(60)
        );
        Auction active = Auction.create(
                AuctionId.newId(),
                new PartyId("seller-2"),
                Lot.singleProductDraft("Sneakers"),
                new Price(new BigDecimal("200.00"), "PLN"),
                now.plusSeconds(3600)
        );
        repository.save(expired);
        repository.save(active);

        CloseExpiredAuctionsService service = new CloseExpiredAuctionsService(repository, events);

        ClosedAuctionsResult result = service.closeExpiredAuctions(new CloseExpiredAuctionsCommand(now));

        assertEquals(1, result.closedCount());
        assertEquals(0, result.conflictCount());
        assertEquals(1, events.events.size());
        assertInstanceOf(AuctionClosedWithoutWinnerEvent.class, events.events.getFirst());
    }

    @Test
    void shouldCountConflictAndContinueWhenClosingExpiredAuctions() {
        Instant now = Instant.parse("2026-03-05T12:00:00Z");
        InMemoryAuctionRepository repository = new InMemoryAuctionRepository();
        repository.failOnSave = true;
        RecordingAuctionEventPublisher events = new RecordingAuctionEventPublisher();

        Auction expired = Auction.create(
                AuctionId.newId(),
                new PartyId("seller-1"),
                Lot.singleProductDraft("Watch"),
                new Price(new BigDecimal("80.00"), "PLN"),
                now.minusSeconds(60)
        );
        repository.storage.put(expired.id(), expired);

        CloseExpiredAuctionsService service = new CloseExpiredAuctionsService(repository, events);

        ClosedAuctionsResult result = service.closeExpiredAuctions(new CloseExpiredAuctionsCommand(now));

        assertEquals(0, result.closedCount());
        assertEquals(1, result.conflictCount());
        assertEquals(1, events.events.size());
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
