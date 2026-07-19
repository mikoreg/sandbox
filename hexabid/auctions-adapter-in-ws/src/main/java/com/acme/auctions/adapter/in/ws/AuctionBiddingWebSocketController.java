package com.acme.auctions.adapter.in.ws;

import com.acme.auctions.auth.core.identityaccess.port.out.CurrentUserProvider;
import com.acme.auctions.core.auctioning.model.AuctionId;
import com.acme.auctions.core.auctioning.model.Price;
import com.acme.auctions.core.auctioning.port.in.PlaceBidCommand;
import com.acme.auctions.core.auctioning.port.in.PlaceBidFailureReason;
import com.acme.auctions.core.auctioning.port.in.PlaceBidResult;
import com.acme.auctions.core.auctioning.port.in.PlaceBidUseCase;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.util.UUID;

@Controller
public class AuctionBiddingWebSocketController {

    private final PlaceBidUseCase placeBidUseCase;
    private final CurrentUserProvider currentUserProvider;
    private final SimpMessagingTemplate messagingTemplate;

    public AuctionBiddingWebSocketController(
            PlaceBidUseCase placeBidUseCase,
            CurrentUserProvider currentUserProvider,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.placeBidUseCase = placeBidUseCase;
        this.currentUserProvider = currentUserProvider;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/auctions/{auctionId}/bids")
    public void placeBid(@DestinationVariable UUID auctionId, PlaceBidWebSocketMessage message) {
        var authenticatedUser = currentUserProvider.maybeCurrentUser().orElse(null);
        if (authenticatedUser == null) {
            messagingTemplate.convertAndSend(
                    "/topic/auctions/" + auctionId + "/errors",
                    new BidRejectedWebSocketMessage("UNAUTHENTICATED", "authentication is required")
            );
            return;
        }
        PlaceBidResult result = placeBidUseCase.placeBid(new PlaceBidCommand(
                new AuctionId(auctionId),
                authenticatedUser.partyId(),
                new Price(new BigDecimal(message.amount()), message.currency())
        ));
        if (!(result instanceof PlaceBidResult.BidRejected(
                PlaceBidFailureReason reason, String message1
        ))) {
            PlaceBidResult.BidAccepted accepted = (PlaceBidResult.BidAccepted) result;
            var placedBid = accepted.bid();

            messagingTemplate.convertAndSend(
                    "/topic/auctions/" + auctionId + "/bids",
                    new BidAcceptedWebSocketMessage(
                            placedBid.bidderId(),
                            placedBid.amount(),
                            placedBid.currency(),
                            placedBid.placedAt().toString()
                    )
            );
        } else {
            messagingTemplate.convertAndSend(
                    "/topic/auctions/" + auctionId + "/errors",
                    new BidRejectedWebSocketMessage(reason.name(), message1)
            );
            return;
        }
    }
}
