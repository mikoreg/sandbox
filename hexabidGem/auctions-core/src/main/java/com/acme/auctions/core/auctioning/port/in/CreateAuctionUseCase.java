package main.java.com.acme.auctions.core.auctioning.port.in;

import main.java.com.acme.auctions.core.auctioning.model.Auction;
import main.java.com.acme.auctions.core.auctioning.model.Price;
import main.java.com.acme.auctions.core.auctioning.port.out.AuctionRepository;
import java.time.Instant;

/**
 * Use case for creating a new auction.
 */
public class CreateAuctionUseCase {
    private final AuctionRepository auctionRepository;

    public CreateAuctionUseCase(AuctionRepository auctionRepository) {
        this.auctionRepository = auctionRepository;
    }

    public Auction createAuction(String title, String description, Price startingPrice, Instant endsAt) {
        Auction auction = Auction.create(title, description, startingPrice, endsAt);
        return auctionRepository.save(auction);
    }
}