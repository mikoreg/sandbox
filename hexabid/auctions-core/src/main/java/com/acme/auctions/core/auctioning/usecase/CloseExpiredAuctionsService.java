package com.acme.auctions.core.auctioning.usecase;

import com.acme.auctions.core.auctioning.exception.AuctionConcurrencyConflictException;
import com.acme.auctions.core.auctioning.model.Auction;
import com.acme.auctions.core.auctioning.port.in.CloseExpiredAuctionsCommand;
import com.acme.auctions.core.auctioning.port.in.CloseExpiredAuctionsUseCase;
import com.acme.auctions.core.auctioning.port.in.ClosedAuctionsResult;
import com.acme.auctions.core.auctioning.port.out.AuctionEventPublisher;
import com.acme.auctions.core.auctioning.port.out.AuctionRepository;

import java.util.Objects;

public final class CloseExpiredAuctionsService implements CloseExpiredAuctionsUseCase {

    private final AuctionRepository auctionRepository;
    private final AuctionEventPublisher eventPublisher;

    public CloseExpiredAuctionsService(AuctionRepository auctionRepository, AuctionEventPublisher eventPublisher) {
        this.auctionRepository = Objects.requireNonNull(auctionRepository, "auctionRepository must not be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher must not be null");
    }

    @Override
    public ClosedAuctionsResult closeExpiredAuctions(CloseExpiredAuctionsCommand command) {
        int closedCount = 0;
        int conflictCount = 0;
        for (Auction auction : auctionRepository.findExpiredOpenAuctions(command.currentTime())) {
            var endedEvent = auction.maybeCloseIfExpired(command.currentTime());
            if (endedEvent.isPresent()) {
                try {
                    endedEvent.ifPresent(eventPublisher::publish);
                    auctionRepository.save(auction);
                    closedCount++;
                } catch (AuctionConcurrencyConflictException exception) {
                    conflictCount++;
                }
            }
        }
        return new ClosedAuctionsResult(closedCount, conflictCount);
    }
}
