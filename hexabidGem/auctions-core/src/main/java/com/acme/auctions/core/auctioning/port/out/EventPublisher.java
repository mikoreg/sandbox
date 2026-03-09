package main.java.com.acme.auctions.core.auctioning.port.out;

import main.java.com.acme.auctions.core.auctioning.event.AuctionEndedEvent;
import main.java.com.acme.auctions.core.auctioning.event.OutbidEvent;

/**
 * Port for publishing domain events.
 */
public interface EventPublisher {
    void publish(AuctionEndedEvent event);
    void publish(OutbidEvent event);
}