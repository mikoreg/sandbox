package com.acme.auctions.adapter.out.kafka;

import com.acme.auctions.core.auctioning.event.AuctionDomainEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Map;

public class KafkaAuctionEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String outbidTopic;
    private final String endedTopic;

    public KafkaAuctionEventPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${auctions.kafka.topics.outbid:auctions.outbid}") String outbidTopic,
            @Value("${auctions.kafka.topics.ended:auctions.ended}") String endedTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.outbidTopic = outbidTopic;
        this.endedTopic = endedTopic;
    }

    public void publish(AuctionDomainEvent event) {
        if (event instanceof com.acme.auctions.core.auctioning.event.AuctionLeaderChangedEvent leaderChangedEvent) {
            kafkaTemplate.send(
                    outbidTopic,
                    leaderChangedEvent.auctionId().toString(),
                    new AuctionEventMessage(
                            leaderChangedEvent.type(),
                            leaderChangedEvent.auctionId().toString(),
                            leaderChangedEvent.occurredAt().toString(),
                            Map.of(
                                    "previousLeaderId", leaderChangedEvent.previousLeaderId() == null ? "" : leaderChangedEvent.previousLeaderId().value(),
                                    "newLeaderId", leaderChangedEvent.newLeaderId().value(),
                                    "newLeadingPrice", leaderChangedEvent.newLeadingPrice().amount().toPlainString(),
                                    "currency", leaderChangedEvent.newLeadingPrice().currency()
                            )
                    )
            );
        }
        if (event instanceof com.acme.auctions.core.auctioning.event.AuctionWonEvent wonEvent) {
            kafkaTemplate.send(
                    endedTopic,
                    wonEvent.auctionId().toString(),
                    new AuctionEventMessage(
                            wonEvent.type(),
                            wonEvent.auctionId().toString(),
                            wonEvent.occurredAt().toString(),
                            Map.of(
                                    "winnerId", wonEvent.winnerId().value(),
                                    "winningPrice", wonEvent.winningPrice().amount().toPlainString(),
                                    "currency", wonEvent.winningPrice().currency()
                            )
                    )
            );
        }
        if (event instanceof com.acme.auctions.core.auctioning.event.AuctionClosedWithoutWinnerEvent closedWithoutWinnerEvent) {
            kafkaTemplate.send(
                    endedTopic,
                    closedWithoutWinnerEvent.auctionId().toString(),
                    new AuctionEventMessage(
                            closedWithoutWinnerEvent.type(),
                            closedWithoutWinnerEvent.auctionId().toString(),
                            closedWithoutWinnerEvent.occurredAt().toString(),
                            Map.of()
                    )
            );
        }
    }
}
