package com.acme.auctions.core.auctioning.port.out;

import com.acme.auctions.core.auctioning.event.AuctionDomainEvent;

public interface AuctionEventPublisher {
    void publish(AuctionDomainEvent event);
}
