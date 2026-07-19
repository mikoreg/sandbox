package main.java.com.acme.auctions.core.auctioning.port.in;

import main.java.com.acme.auctions.core.auctioning.model.Auction;
import main.java.com.acme.auctions.core.auctioning.model.Price;
import main.java.com.acme.auctions.core.auctioning.port.out.AuctionRepository;
import main.java.com.acme.auctions.core.auctioning.port.out.EventPublisher;
import java.util.UUID;

/**
 * Use case for placing a bid on an auction.
 */
public class PlaceBidUseCase {
    private final AuctionRepository auctionRepository;
    private final EventPublisher eventPublisher;

    public PlaceBidUseCase(AuctionRepository auctionRepository, EventPublisher eventPublisher) {
        this.auctionRepository = auctionRepository;
        this.eventPublisher = eventPublisher;
    }

    public void placeBid(UUID auctionId, UUID bidderId, Price bidAmount) {
        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new IllegalArgumentException("Auction not found"));

        if (auction.getStatus() != AuctionStatus.PUBLISHED) {
            throw new IllegalStateException("Auction is not active");
        }

        if (bidAmount.amount.compareTo(auction.getStartingPrice().amount) < 0) {
            throw new IllegalArgumentException("Bid amount is too low");
        }

        // Business logic for placing bid would go here
        // For now, we'll just update the auction status to show it was bid on
        auction = auction.toBuilder()
            .status(AuctionStatus.BID_ON)
            .build();

        auctionRepository.save(auction);
        eventPublisher.publish(new OutbidEvent(auctionId, bidderId, Instant.now()));
    }
}