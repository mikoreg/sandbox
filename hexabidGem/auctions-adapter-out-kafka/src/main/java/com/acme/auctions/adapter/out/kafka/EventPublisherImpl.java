package main.java.com.acme.auctions.adapter.out.kafka;

import com.acme.auctions.core.auctioning.event.AuctionEndedEvent;
import com.acme.auctions.core.auctioning.event.OutbidEvent;
import com.acme.auctions.core.auctioning.port.out.EventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventPublisherImpl implements EventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public EventPublisherImpl(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(AuctionEndedEvent event) {
        kafkaTemplate.send("auction-ended", event);
    }

    @Override
    public void publish(OutbidEvent event) {
        kafkaTemplate.send("outbid", event);
    }
}