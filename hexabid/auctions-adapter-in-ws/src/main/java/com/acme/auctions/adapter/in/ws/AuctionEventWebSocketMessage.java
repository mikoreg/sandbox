package com.acme.auctions.adapter.in.ws;

import java.util.Map;

public record AuctionEventWebSocketMessage(String type, String auctionId, Map<String, Object> payload, String occurredAt) {
}
