package com.acme.auctions.core.auctioning.usecase;

import com.acme.auctions.core.auctioning.exception.AuctionEndTimeMustBeInFutureException;
import com.acme.auctions.core.auctioning.model.Auction;
import com.acme.auctions.core.auctioning.model.AuctionId;
import com.acme.auctions.core.auctioning.port.in.CreateAuctionCommand;
import com.acme.auctions.core.auctioning.port.in.CreateAuctionFailureReason;
import com.acme.auctions.core.auctioning.port.in.CreateAuctionResult;
import com.acme.auctions.core.auctioning.port.in.CreateAuctionUseCase;
import com.acme.auctions.core.auctioning.port.out.AuctionRepository;
import com.acme.auctions.core.auctioning.port.out.KycClient;
import com.acme.auctions.core.lot.model.Lot;

import java.time.Clock;
import java.util.Objects;

public final class CreateAuctionService implements CreateAuctionUseCase {

    private final AuctionRepository auctionRepository;
    private final KycClient kycClient;
    private final Clock clock;

    public CreateAuctionService(AuctionRepository auctionRepository, KycClient kycClient, Clock clock) {
        this.auctionRepository = Objects.requireNonNull(auctionRepository, "auctionRepository must not be null");
        this.kycClient = Objects.requireNonNull(kycClient, "kycClient must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public CreateAuctionResult createAuction(CreateAuctionCommand command) {
        if (!kycClient.isVerified(command.sellerId())) {
            return new CreateAuctionResult.AuctionCreationRejected(
                    CreateAuctionFailureReason.SELLER_NOT_VERIFIED,
                    "seller must pass KYC before creating auction"
            );
        }
        if (!command.endsAt().isAfter(clock.instant())) {
            AuctionEndTimeMustBeInFutureException exception = new AuctionEndTimeMustBeInFutureException();
            return new CreateAuctionResult.AuctionCreationRejected(
                    CreateAuctionFailureReason.AUCTION_END_TIME_NOT_IN_FUTURE,
                    exception.getMessage()
            );
        }

        Auction created = Auction.create(
                AuctionId.newId(),
                command.sellerId(),
                Lot.singleProductDraft(command.title()),
                command.startingPrice(),
                command.endsAt()
        );
        return new CreateAuctionResult.AuctionCreated(AuctionViews.from(auctionRepository.save(created)));
    }
}
