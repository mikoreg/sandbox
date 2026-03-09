package com.acme.auctions.core.auctioning.model;

import com.acme.auctions.core.auctioning.event.AuctionClosedWithoutWinnerEvent;
import com.acme.auctions.core.auctioning.event.AuctionDomainEvent;
import com.acme.auctions.core.auctioning.event.AuctionLeaderChangedEvent;
import com.acme.auctions.core.auctioning.event.AuctionWonEvent;
import com.acme.auctions.core.auctioning.exception.AuctionClosedForBiddingException;
import com.acme.auctions.core.auctioning.exception.AuctionExpiredForBiddingException;
import com.acme.auctions.core.auctioning.exception.BidAmountTooLowException;
import com.acme.auctions.core.auctioning.exception.SellerCannotBidOnOwnAuctionException;
import com.acme.auctions.core.lot.model.Lot;
import com.acme.auctions.core.party.model.PartyId;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class Auction {

    private final AuctionId id;
    private final PartyId sellerId;
    private final Lot lot;
    private final Price startingPrice;
    private final Instant endsAt;
    private final @Nullable Long version;
    private AuctionStatus status;
    private final List<Bid> biddingHistory;

    private Auction(
            AuctionId id,
            PartyId sellerId,
            Lot lot,
            Price startingPrice,
            Instant endsAt,
            @Nullable Long version,
            AuctionStatus status,
            List<Bid> biddingHistory
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.sellerId = Objects.requireNonNull(sellerId, "sellerId must not be null");
        this.lot = Objects.requireNonNull(lot, "lot must not be null");
        this.startingPrice = Objects.requireNonNull(startingPrice, "startingPrice must not be null");
        this.endsAt = Objects.requireNonNull(endsAt, "endsAt must not be null");
        this.version = version;
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.biddingHistory = new ArrayList<>(Objects.requireNonNull(biddingHistory, "biddingHistory must not be null"));
    }

    public static Auction create(AuctionId id, PartyId sellerId, Lot lot, Price startingPrice, Instant endsAt) {
        return new Auction(id, sellerId, lot, startingPrice, endsAt, null, AuctionStatus.OPEN, List.of());
    }

    public static Auction rehydrate(
            AuctionId id,
            PartyId sellerId,
            Lot lot,
            Price startingPrice,
            Instant endsAt,
            @Nullable Long version,
            AuctionStatus status,
            List<Bid> biddingHistory
    ) {
        return new Auction(id, sellerId, lot, startingPrice, endsAt, version, status, biddingHistory);
    }

    public PlaceBidDecision placeBid(PartyId bidderId, Price amount, Instant placedAt) {
        ensureOpenAt(placedAt);
        Objects.requireNonNull(bidderId, "bidderId must not be null");
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(placedAt, "placedAt must not be null");
        if (sellerId.equals(bidderId)) {
            throw new SellerCannotBidOnOwnAuctionException();
        }
        if (!amount.isGreaterThan(currentPrice())) {
            throw new BidAmountTooLowException();
        }

        Bid previousLeader = maybeLeadingBid().orElse(null);
        Bid bid = new Bid(bidderId, amount, placedAt);
        biddingHistory.add(bid);

        PartyId previousLeaderId = previousLeader == null ? null : previousLeader.bidderId();
        Optional<AuctionLeaderChangedEvent> leaderChangedEvent = Optional.of(new AuctionLeaderChangedEvent(
                id,
                previousLeaderId,
                bidderId,
                amount,
                placedAt
        ));

        return new PlaceBidDecision(bid, leaderChangedEvent);
    }

    public Optional<AuctionDomainEvent> maybeCloseIfExpired(Instant now) {
        Objects.requireNonNull(now, "now must not be null");
        if (status == AuctionStatus.CLOSED || now.isBefore(endsAt)) {
            return Optional.empty();
        }
        status = AuctionStatus.CLOSED;
        return maybeWinnerId()
                .<AuctionDomainEvent>map(winner -> new AuctionWonEvent(id, winner, currentPrice(), now))
                .or(() -> Optional.of(new AuctionClosedWithoutWinnerEvent(id, now)));
    }

    public AuctionId id() {
        return id;
    }

    public PartyId sellerId() {
        return sellerId;
    }

    public String title() {
        return lot.title();
    }

    public Lot lot() {
        return lot;
    }

    public Price startingPrice() {
        return startingPrice;
    }

    public Instant endsAt() {
        return endsAt;
    }

    public Optional<Long> maybeVersion() {
        return Optional.ofNullable(version);
    }

    public AuctionStatus status() {
        return status;
    }

    public List<Bid> biddingHistory() {
        return List.copyOf(biddingHistory);
    }

    public Price currentPrice() {
        return maybeLeadingBid().map(Bid::amount).orElse(startingPrice);
    }

    public Optional<PartyId> maybeWinnerId() {
        return maybeLeadingBid().map(Bid::bidderId);
    }

    public boolean isExpiredAt(Instant now) {
        return !now.isBefore(endsAt);
    }

    private Optional<Bid> maybeLeadingBid() {
        if (biddingHistory.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(biddingHistory.getLast());
    }

    private void ensureOpenAt(Instant instant) {
        if (status == AuctionStatus.CLOSED) {
            throw new AuctionClosedForBiddingException();
        }
        if (!instant.isBefore(endsAt)) {
            throw new AuctionExpiredForBiddingException();
        }
    }
    public record PlaceBidDecision(Bid acceptedBid, Optional<AuctionLeaderChangedEvent> leaderChangedEvent) {
    }
}
