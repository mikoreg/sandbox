package com.acme.auctions.adapter.in.rest;

import com.acme.auctions.auth.core.identityaccess.port.in.FindCurrentUserProfileUseCase;
import com.acme.auctions.auth.core.identityaccess.port.out.CurrentUserProvider;
import com.acme.auctions.contract.model.AuctionListResponse;
import com.acme.auctions.contract.api.AuctionsApiDelegate;
import com.acme.auctions.contract.model.AuctionResponse;
import com.acme.auctions.contract.model.AuctionSort;
import com.acme.auctions.contract.model.AuctionStatus;
import com.acme.auctions.contract.model.CreateAuctionRequest;
import com.acme.auctions.contract.model.CurrentUserProfileResponse;
import com.acme.auctions.core.auctioning.model.AuctionId;
import com.acme.auctions.core.auctioning.model.Price;
import com.acme.auctions.core.auctioning.port.in.AuctionDetailsResult;
import com.acme.auctions.core.auctioning.port.in.BrowseAuctionsQuery;
import com.acme.auctions.core.auctioning.port.in.BrowseAuctionsUseCase;
import com.acme.auctions.core.auctioning.port.in.CreateAuctionResult;
import com.acme.auctions.core.auctioning.port.in.CreateAuctionCommand;
import com.acme.auctions.core.auctioning.port.in.CreateAuctionUseCase;
import com.acme.auctions.core.auctioning.port.in.FindAuctionDetailsUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class RestAuctionApiDelegate implements AuctionsApiDelegate {

    private final CreateAuctionUseCase createAuctionUseCase;
    private final FindAuctionDetailsUseCase findAuctionDetailsUseCase;
    private final BrowseAuctionsUseCase browseAuctionsUseCase;
    private final FindCurrentUserProfileUseCase findCurrentUserProfileUseCase;
    private final CurrentUserProvider currentUserProvider;
    private final RestAuctionContractMapper mapper;

    public RestAuctionApiDelegate(
            CreateAuctionUseCase createAuctionUseCase,
            FindAuctionDetailsUseCase findAuctionDetailsUseCase,
            BrowseAuctionsUseCase browseAuctionsUseCase,
            FindCurrentUserProfileUseCase findCurrentUserProfileUseCase,
            CurrentUserProvider currentUserProvider,
            RestAuctionContractMapper mapper
    ) {
        this.createAuctionUseCase = createAuctionUseCase;
        this.findAuctionDetailsUseCase = findAuctionDetailsUseCase;
        this.browseAuctionsUseCase = browseAuctionsUseCase;
        this.findCurrentUserProfileUseCase = findCurrentUserProfileUseCase;
        this.currentUserProvider = currentUserProvider;
        this.mapper = mapper;
    }

    @Override
    public ResponseEntity<AuctionListResponse> browseAuctions(String query, AuctionStatus status, AuctionSort sort, Integer limit, String after) {
        return ResponseEntity.ok(mapper.toResponse(browseAuctionsUseCase.browseAuctions(toQuery(query, status, sort, limit, after))));
    }

    @Override
    public ResponseEntity<AuctionResponse> createAuction(CreateAuctionRequest request) {
        var authenticatedUser = currentUserProvider.maybeCurrentUser().orElse(null);
        if (authenticatedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        CreateAuctionResult result = createAuctionUseCase.createAuction(new CreateAuctionCommand(
                authenticatedUser.partyId(),
                request.getTitle(),
                toPrice(request.getStartingPrice().getAmount(), request.getStartingPrice().getCurrency()),
                request.getEndsAt().toInstant()
        ));
        if (result instanceof CreateAuctionResult.AuctionCreated created) {
            return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(created.auction()));
        }
        CreateAuctionResult.AuctionCreationRejected rejected = (CreateAuctionResult.AuctionCreationRejected) result;
        throw new RestRequestRejectedException(HttpStatus.BAD_REQUEST, rejected.message());
    }

    @Override
    public ResponseEntity<AuctionResponse> getAuctionById(UUID auctionId) {
        AuctionDetailsResult result = findAuctionDetailsUseCase.findAuctionDetails(new AuctionId(auctionId));
        if (result instanceof AuctionDetailsResult.AuctionFound found) {
            return ResponseEntity.ok(mapper.toResponse(found.auction()));
        }
        return ResponseEntity.notFound().build();
    }

    @Override
    public ResponseEntity<CurrentUserProfileResponse> getCurrentUserProfile() {
        return findCurrentUserProfileUseCase.findCurrentUserProfile()
                .map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @Override
    public ResponseEntity<AuctionListResponse> browseMyAuctions(AuctionStatus status, AuctionSort sort, Integer limit, String after) {
        var authenticatedUser = currentUserProvider.maybeCurrentUser().orElse(null);
        if (authenticatedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(
                mapper.toResponse(
                        browseAuctionsUseCase.browseSellerAuctions(
                                authenticatedUser.partyId(),
                                toQuery(null, status, sort, limit, after)
                        )
                )
        );
    }

    @Override
    public ResponseEntity<AuctionListResponse> browseMyBids(AuctionStatus status, AuctionSort sort, Integer limit, String after) {
        var authenticatedUser = currentUserProvider.maybeCurrentUser().orElse(null);
        if (authenticatedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(
                mapper.toResponse(
                        browseAuctionsUseCase.browseBidderAuctions(
                                authenticatedUser.partyId(),
                                toQuery(null, status, sort, limit, after)
                        )
                )
        );
    }

    private Price toPrice(String amount, String currency) {
        return new Price(new BigDecimal(amount), currency);
    }

    private BrowseAuctionsQuery toQuery(String query, AuctionStatus status, AuctionSort sort, Integer limit, String after) {
        com.acme.auctions.core.auctioning.model.AuctionStatus parsedStatus =
                status == null ? null : com.acme.auctions.core.auctioning.model.AuctionStatus.valueOf(status.getValue());
        com.acme.auctions.core.auctioning.port.in.AuctionSort parsedSort =
                sort == null
                        ? com.acme.auctions.core.auctioning.port.in.AuctionSort.ENDING_SOON
                        : com.acme.auctions.core.auctioning.port.in.AuctionSort.valueOf(sort.getValue());
        int parsedLimit = limit == null ? 20 : limit;
        return new BrowseAuctionsQuery(query, parsedStatus, parsedSort, parsedLimit, after);
    }
}
