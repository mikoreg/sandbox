package com.acme.auctions.core.auctioning.port.in;

import java.util.Objects;

public sealed interface PlaceBidResult permits PlaceBidResult.BidAccepted, PlaceBidResult.BidRejected {

    record BidAccepted(BidView bid) implements PlaceBidResult {

        public BidAccepted {
            Objects.requireNonNull(bid, "bid must not be null");
        }
    }

    record BidRejected(PlaceBidFailureReason reason, String message) implements PlaceBidResult {

        public BidRejected {
            Objects.requireNonNull(reason, "reason must not be null");
            Objects.requireNonNull(message, "message must not be null");
        }
    }
}
