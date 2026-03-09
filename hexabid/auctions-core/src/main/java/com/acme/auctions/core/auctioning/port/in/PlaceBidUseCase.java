package com.acme.auctions.core.auctioning.port.in;

public interface PlaceBidUseCase {
    PlaceBidResult placeBid(PlaceBidCommand command);
}
