package com.acme.auctions.core.auctioning.port.in;

import java.time.Instant;

public record BidView(String bidderId, String amount, String currency, Instant placedAt) {
}
