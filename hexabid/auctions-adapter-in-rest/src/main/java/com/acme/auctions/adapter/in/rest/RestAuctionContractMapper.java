package com.acme.auctions.adapter.in.rest;

import com.acme.auctions.auth.core.identityaccess.port.in.CurrentUserProfileView;
import com.acme.auctions.contract.model.AuctionListItemResponse;
import com.acme.auctions.contract.model.AuctionListResponse;
import com.acme.auctions.contract.model.AuctionResponse;
import com.acme.auctions.contract.model.AuctionStatus;
import com.acme.auctions.contract.model.BidResponse;
import com.acme.auctions.contract.model.CurrentUserProfileResponse;
import com.acme.auctions.core.auctioning.port.in.AuctionView;
import com.acme.auctions.core.auctioning.port.in.AuctionBrowsePage;
import com.acme.auctions.core.auctioning.port.in.AuctionSummaryView;
import com.acme.auctions.core.auctioning.port.in.BidView;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Component
class RestAuctionContractMapper {

    AuctionListResponse toResponse(AuctionBrowsePage page) {
        return new AuctionListResponse(page.items().stream().map(this::toResponse).toList())
                .nextCursor(page.nextCursor());
    }

    AuctionResponse toResponse(AuctionView view) {
        return new AuctionResponse(
                view.auctionId(),
                view.sellerId(),
                view.title(),
                view.currentPrice(),
                view.currency(),
                OffsetDateTime.ofInstant(view.endsAt(), ZoneOffset.UTC),
                AuctionStatus.fromValue(view.status().name()),
                view.biddingHistory().stream().map(this::toResponse).toList()
        ).leadingBidderId(view.leadingBidderId());
    }

    AuctionListItemResponse toResponse(AuctionSummaryView view) {
        return new AuctionListItemResponse(
                view.auctionId(),
                view.sellerId(),
                view.title(),
                view.currentPrice(),
                view.currency(),
                OffsetDateTime.ofInstant(view.endsAt(), ZoneOffset.UTC),
                AuctionStatus.fromValue(view.status().name())
        ).leadingBidderId(view.leadingBidderId());
    }

    BidResponse toResponse(BidView view) {
        return new BidResponse(
                view.bidderId(),
                view.amount(),
                view.currency(),
                OffsetDateTime.ofInstant(view.placedAt(), ZoneOffset.UTC)
        );
    }

    CurrentUserProfileResponse toResponse(CurrentUserProfileView view) {
        return new CurrentUserProfileResponse(
                view.partyId(),
                view.provider(),
                view.displayName(),
                view.verified()
        ).email(view.email());
    }
}
