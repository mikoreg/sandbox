package com.acme.auctions.core.auctioning.model;

import java.time.Instant;
import java.util.UUID;
import java.util.Objects;

/**
 * Represents the Auction Aggregate Root.
 * It encapsulates the state and business rules for an auction.
 * Note the absence of any framework-specific annotations (JPA, Spring).
 */
public class Auction {

    private final UUID id;
    private final String title;
    private final String description;
    private final Price startingPrice;
    private final Instant endsAt;
    private AuctionStatus status;

    private Auction(UUID id, String title, String description, Price startingPrice, Instant endsAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startingPrice = startingPrice;
        this.endsAt = endsAt;
        this.status = AuctionStatus.PUBLISHED; // Initial status
    }

    /**
     * Factory method to create a new Auction.
     * Enforces invariants.
     */
    public static Auction create(String title, String description, Price startingPrice, Instant endsAt) {
        Objects.requireNonNull(title, "Title cannot be null");
        Objects.requireNonNull(description, "Description cannot be null");
        Objects.requireNonNull(startingPrice, "Starting price cannot be null");
        Objects.requireNonNull(endsAt, "End date cannot be null");

        if (endsAt.isBefore(Instant.now())) {
            throw new IllegalArgumentException("Auction end date must be in the future.");
        }

        return new Auction(UUID.randomUUID(), title, description, startingPrice, endsAt);
    }

    // Getters for immutable fields
    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Price getStartingPrice() {
        return startingPrice;
    }

    public Instant getEndsAt() {
        return endsAt;
    }

    public AuctionStatus getStatus() {
        return status;
    }
    
    // Business methods would go here, e.g., placeBid, close, etc.
}
