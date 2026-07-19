package com.acme.auctions.core.auctioning.port.in;

import com.acme.auctions.core.auctioning.model.Price;
import com.acme.auctions.core.party.model.PartyId;

import java.time.Instant;
import java.util.Objects;

public record CreateAuctionCommand(PartyId sellerId, String title, Price startingPrice, Instant endsAt) {

    public CreateAuctionCommand {
        Objects.requireNonNull(sellerId, "sellerId must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(startingPrice, "startingPrice must not be null");
        Objects.requireNonNull(endsAt, "endsAt must not be null");
    }
}
