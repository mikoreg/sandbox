package com.acme.auctions.adapter.in.ws;

import com.acme.auctions.core.auctioning.event.AuctionClosedWithoutWinnerEvent;
import com.acme.auctions.core.auctioning.event.AuctionDomainEvent;
import com.acme.auctions.core.auctioning.event.AuctionLeaderChangedEvent;
import com.acme.auctions.core.auctioning.event.AuctionWonEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Map;

public class AuctionWebSocketEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public AuctionWebSocketEventPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publish(AuctionDomainEvent event) {
        messagingTemplate.convertAndSend(
                "/topic/auctions/" + event.auctionId() + "/events",
                toMessage(event)
        );
    }

    private AuctionEventWebSocketMessage toMessage(AuctionDomainEvent event) {
        if (event instanceof AuctionLeaderChangedEvent leaderChangedEvent) {
            return new AuctionEventWebSocketMessage(
                    leaderChangedEvent.type(),
                    leaderChangedEvent.auctionId().toString(),
                    Map.of(
                            "previousLeaderId", leaderChangedEvent.previousLeaderId() == null ? "" : leaderChangedEvent.previousLeaderId().value(),
                            "newLeaderId", leaderChangedEvent.newLeaderId().value(),
                            "newLeadingPrice", leaderChangedEvent.newLeadingPrice().amount().toPlainString(),
                            "currency", leaderChangedEvent.newLeadingPrice().currency()
                    ),
                    leaderChangedEvent.occurredAt().toString()
            );
        }
        if (event instanceof AuctionWonEvent wonEvent) {
            return new AuctionEventWebSocketMessage(
                    wonEvent.type(),
                    wonEvent.auctionId().toString(),
                    Map.of(
                            "winnerId", wonEvent.winnerId().value(),
                            "winningPrice", wonEvent.winningPrice().amount().toPlainString(),
                            "currency", wonEvent.winningPrice().currency()
                    ),
                    wonEvent.occurredAt().toString()
            );
        }
        if (event instanceof AuctionClosedWithoutWinnerEvent closedWithoutWinnerEvent) {
            return new AuctionEventWebSocketMessage(
                    closedWithoutWinnerEvent.type(),
                    closedWithoutWinnerEvent.auctionId().toString(),
                    Map.of(),
                    closedWithoutWinnerEvent.occurredAt().toString()
            );
        }
        throw new IllegalArgumentException("Unsupported event type: " + event.getClass().getName());
    }
}
