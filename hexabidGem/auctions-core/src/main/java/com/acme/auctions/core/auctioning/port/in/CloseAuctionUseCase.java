package main.java.com.acme.auctions.core.auctioning.port.in;

import main.java.com.acme.auctions.core.auctioning.model.Auction;
import main.java.com.acme.auctions.core.auctioning.port.out.AuctionRepository;
import main.java.com.acme.auctions.core.auctioning.port.out.EventPublisher;
import java.util.UUID;

/**
 * Use case for closing an auction.
 */
public class CloseAuctionUseCase {
    private final AuctionRepository auctionRepository;
    private final EventPublisher eventPublisher;

    public CloseAuctionUseCase(AuctionRepository auctionRepository, EventPublisher eventPublisher) {
        this.auctionRepository = auctionRepository;
        this.eventPublisher = eventPublisher;
    }

    public void closeAuction(UUID auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new IllegalArgumentException("Auction not found"));

        if (auction.getStatus() == AuctionStatus.CLOSED) {
            throw new IllegalStateException("Auction is already closed");
        }

        auction = auction.toBuilder()
            .status(AuctionStatus.CLOSED)
            .build();

        auctionRepository.save(auction);
        eventPublisher.publish(new AuctionEndedEvent(auctionId, Instant.now()));
    }
}