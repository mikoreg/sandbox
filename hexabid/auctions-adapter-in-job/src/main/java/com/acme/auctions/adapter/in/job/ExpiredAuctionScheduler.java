package com.acme.auctions.adapter.in.job;

import com.acme.auctions.core.auctioning.port.in.CloseExpiredAuctionsCommand;
import com.acme.auctions.core.auctioning.port.in.CloseExpiredAuctionsUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;

@Component
public class ExpiredAuctionScheduler {

    private final CloseExpiredAuctionsUseCase closeExpiredAuctionsUseCase;
    private final Clock clock;

    public ExpiredAuctionScheduler(CloseExpiredAuctionsUseCase closeExpiredAuctionsUseCase, Clock clock) {
        this.closeExpiredAuctionsUseCase = closeExpiredAuctionsUseCase;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${auctions.jobs.close-expired.delay:60000}")
    public void checkExpiredAuctions() {
        closeExpiredAuctionsUseCase.closeExpiredAuctions(new CloseExpiredAuctionsCommand(clock.instant()));
    }
}
