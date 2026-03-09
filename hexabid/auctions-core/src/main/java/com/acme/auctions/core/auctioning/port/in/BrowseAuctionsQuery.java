package com.acme.auctions.core.auctioning.port.in;

import com.acme.auctions.core.auctioning.model.AuctionStatus;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public record BrowseAuctionsQuery(
        @Nullable String text,
        @Nullable AuctionStatus status,
        AuctionSort sort,
        int limit,
        @Nullable String after
) {

    public BrowseAuctionsQuery {
        Objects.requireNonNull(sort, "sort must not be null");
        if (limit < 1 || limit > 50) {
            throw new IllegalArgumentException("limit must be between 1 and 50");
        }
    }
}
