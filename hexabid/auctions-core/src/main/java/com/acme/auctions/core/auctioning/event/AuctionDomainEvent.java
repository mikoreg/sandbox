package com.acme.auctions.core.auctioning.event;

import com.acme.auctions.core.auctioning.model.AuctionId;

import java.time.Instant;

public sealed interface AuctionDomainEvent permits AuctionLeaderChangedEvent, AuctionWonEvent, AuctionClosedWithoutWinnerEvent {
    AuctionId auctionId();

    Instant occurredAt();

    String type();
}
