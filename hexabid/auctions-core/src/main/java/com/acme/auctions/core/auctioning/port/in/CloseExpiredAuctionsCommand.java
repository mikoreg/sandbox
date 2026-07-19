package com.acme.auctions.core.auctioning.port.in;

import java.time.Instant;
import java.util.Objects;

public record CloseExpiredAuctionsCommand(Instant currentTime) {

    public CloseExpiredAuctionsCommand {
        Objects.requireNonNull(currentTime, "currentTime must not be null");
    }
}
