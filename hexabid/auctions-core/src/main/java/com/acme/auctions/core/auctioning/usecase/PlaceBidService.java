package com.acme.auctions.core.auctioning.usecase;

import com.acme.auctions.core.auctioning.exception.AuctionClosedForBiddingException;
import com.acme.auctions.core.auctioning.exception.AuctionConcurrencyConflictException;
import com.acme.auctions.core.auctioning.exception.AuctionExpiredForBiddingException;
import com.acme.auctions.core.auctioning.exception.BidAmountTooLowException;
import com.acme.auctions.core.auctioning.exception.CurrencyMismatchException;
import com.acme.auctions.core.auctioning.exception.SellerCannotBidOnOwnAuctionException;
import com.acme.auctions.core.auctioning.model.Auction;
import com.acme.auctions.core.auctioning.port.in.PlaceBidCommand;
import com.acme.auctions.core.auctioning.port.in.PlaceBidFailureReason;
import com.acme.auctions.core.auctioning.port.in.PlaceBidResult;
import com.acme.auctions.core.auctioning.port.in.PlaceBidUseCase;
import com.acme.auctions.core.auctioning.port.out.AuctionEventPublisher;
import com.acme.auctions.core.auctioning.port.out.AuctionRepository;
import com.acme.auctions.core.auctioning.port.out.KycClient;

import java.time.Clock;
import java.util.Objects;

public final class PlaceBidService implements PlaceBidUseCase {

    private final AuctionRepository auctionRepository;
    private final KycClient kycClient;
    private final AuctionEventPublisher eventPublisher;
    private final Clock clock;

    public PlaceBidService(
            AuctionRepository auctionRepository,
            KycClient kycClient,
            AuctionEventPublisher eventPublisher,
            Clock clock
    ) {
        this.auctionRepository = Objects.requireNonNull(auctionRepository, "auctionRepository must not be null");
        this.kycClient = Objects.requireNonNull(kycClient, "kycClient must not be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public PlaceBidResult placeBid(PlaceBidCommand command) {
        if (!kycClient.isVerified(command.bidderId())) {
            return rejected(PlaceBidFailureReason.BIDDER_NOT_VERIFIED, "bidder must pass KYC before placing bid");
        }

        Auction auction = auctionRepository.findById(command.auctionId())
                .orElse(null);
        if (auction == null) {
            return rejected(PlaceBidFailureReason.AUCTION_NOT_FOUND, "auction not found");
        }

        try {
            Auction.PlaceBidDecision decision = auction.placeBid(command.bidderId(), command.amount(), clock.instant());
            auctionRepository.save(auction);
            decision.leaderChangedEvent().ifPresent(eventPublisher::publish);
            return new PlaceBidResult.BidAccepted(AuctionViews.from(decision.acceptedBid()));
        } catch (AuctionClosedForBiddingException exception) {
            return rejected(PlaceBidFailureReason.AUCTION_ALREADY_CLOSED, exception.getMessage());
        } catch (AuctionExpiredForBiddingException exception) {
            return rejected(PlaceBidFailureReason.AUCTION_ALREADY_EXPIRED, exception.getMessage());
        } catch (SellerCannotBidOnOwnAuctionException exception) {
            return rejected(PlaceBidFailureReason.SELLER_CANNOT_BID_ON_OWN_AUCTION, exception.getMessage());
        } catch (BidAmountTooLowException exception) {
            return rejected(PlaceBidFailureReason.BID_AMOUNT_TOO_LOW, exception.getMessage());
        } catch (CurrencyMismatchException exception) {
            return rejected(PlaceBidFailureReason.CURRENCY_MISMATCH, exception.getMessage());
        } catch (AuctionConcurrencyConflictException exception) {
            return rejected(PlaceBidFailureReason.CONCURRENT_MODIFICATION, exception.getMessage());
        }
    }

    private static PlaceBidResult.BidRejected rejected(PlaceBidFailureReason reason, String message) {
        return new PlaceBidResult.BidRejected(reason, message);
    }
}
