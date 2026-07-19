package com.acme.auctions.core.auctioning.port.in;

import java.util.Objects;

public sealed interface CreateAuctionResult permits CreateAuctionResult.AuctionCreated, CreateAuctionResult.AuctionCreationRejected {

    record AuctionCreated(AuctionView auction) implements CreateAuctionResult {

        public AuctionCreated {
            Objects.requireNonNull(auction, "auction must not be null");
        }
    }

    record AuctionCreationRejected(CreateAuctionFailureReason reason, String message) implements CreateAuctionResult {

        public AuctionCreationRejected {
            Objects.requireNonNull(reason, "reason must not be null");
            Objects.requireNonNull(message, "message must not be null");
        }
    }
}
