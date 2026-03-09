package com.acme.auctions.adapter.out.kafka;

import java.util.Map;

public record AuctionEventMessage(String type, String auctionId, String occurredAt, Map<String, Object> payload) {
}
