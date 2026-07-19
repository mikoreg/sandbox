package com.acme.auctions.core.auctioning.port.in;

import com.acme.auctions.core.auctioning.model.AuctionId;
import com.acme.auctions.core.auctioning.model.Price;
import com.acme.auctions.core.party.model.PartyId;

import java.util.Objects;

public record PlaceBidCommand(AuctionId auctionId, PartyId bidderId, Price amount) {

    public PlaceBidCommand {
        Objects.requireNonNull(auctionId, "auctionId must not be null");
        Objects.requireNonNull(bidderId, "bidderId must not be null");
        Objects.requireNonNull(amount, "amount must not be null");
    }
}
