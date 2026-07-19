package com.acme.auctions.adapter.in.ws;

public record BidAcceptedWebSocketMessage(String bidderId, String amount, String currency, String placedAt) {
}
