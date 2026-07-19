package main.java.com.acme.auctions.core.auctioning.port.out;

import main.java.com.acme.auctions.core.auctioning.model.Auction;
import java.util.Optional;
import java.util.UUID;

/**
 * Port for auction persistence operations.
 */
public interface AuctionRepository {
    Auction save(Auction auction);
    Optional<Auction> findById(UUID id);
    void deleteById(UUID id);
}