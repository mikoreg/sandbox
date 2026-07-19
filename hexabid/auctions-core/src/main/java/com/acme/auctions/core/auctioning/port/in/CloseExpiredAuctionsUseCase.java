package com.acme.auctions.core.auctioning.port.in;

public interface CloseExpiredAuctionsUseCase {
    ClosedAuctionsResult closeExpiredAuctions(CloseExpiredAuctionsCommand command);
}
