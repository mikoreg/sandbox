package com.acme.auctions.contract.api;

import com.acme.auctions.contract.model.AuctionListResponse;
import com.acme.auctions.contract.model.AuctionResponse;
import com.acme.auctions.contract.model.AuctionSort;
import com.acme.auctions.contract.model.AuctionStatus;
import com.acme.auctions.contract.model.CreateAuctionRequest;
import com.acme.auctions.contract.model.CurrentUserProfileResponse;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import jakarta.annotation.Generated;

/**
 * A delegate to be called by the {@link AuctionsApiController}}.
 * Implement this interface with a {@link org.springframework.stereotype.Service} annotated class.
 */
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-07T00:19:55.094556016+01:00[Europe/Warsaw]", comments = "Generator version: 7.14.0")
public interface AuctionsApiDelegate {

    default Optional<NativeWebRequest> getRequest() {
        return Optional.empty();
    }

    /**
     * GET /api/auctions : Browse auctions for search and listing screens
     *
     * @param query  (optional)
     * @param status  (optional)
     * @param sort  (optional)
     * @param limit  (optional, default to 20)
     * @param after  (optional)
     * @return Auction list page (status code 200)
     * @see AuctionsApi#browseAuctions
     */
    default ResponseEntity<AuctionListResponse> browseAuctions(String query,
        AuctionStatus status,
        AuctionSort sort,
        Integer limit,
        String after) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType: MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString = "{ \"nextCursor\" : \"nextCursor\", \"items\" : [ { \"auctionId\" : \"046b6c7f-0b8a-43b9-b35d-6489e6daee91\", \"sellerId\" : \"sellerId\", \"leadingBidderId\" : \"leadingBidderId\", \"currentPrice\" : \"currentPrice\", \"currency\" : \"currency\", \"title\" : \"title\", \"endsAt\" : \"2000-01-23T04:56:07.000+00:00\", \"status\" : \"OPEN\" }, { \"auctionId\" : \"046b6c7f-0b8a-43b9-b35d-6489e6daee91\", \"sellerId\" : \"sellerId\", \"leadingBidderId\" : \"leadingBidderId\", \"currentPrice\" : \"currentPrice\", \"currency\" : \"currency\", \"title\" : \"title\", \"endsAt\" : \"2000-01-23T04:56:07.000+00:00\", \"status\" : \"OPEN\" } ] }";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
            }
        });
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }

    /**
     * GET /api/me/auctions : Browse auctions created by current user
     *
     * @param status  (optional)
     * @param sort  (optional)
     * @param limit  (optional, default to 20)
     * @param after  (optional)
     * @return Current user auctions (status code 200)
     *         or Authentication required (status code 401)
     * @see AuctionsApi#browseMyAuctions
     */
    default ResponseEntity<AuctionListResponse> browseMyAuctions(AuctionStatus status,
        AuctionSort sort,
        Integer limit,
        String after) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType: MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString = "{ \"nextCursor\" : \"nextCursor\", \"items\" : [ { \"auctionId\" : \"046b6c7f-0b8a-43b9-b35d-6489e6daee91\", \"sellerId\" : \"sellerId\", \"leadingBidderId\" : \"leadingBidderId\", \"currentPrice\" : \"currentPrice\", \"currency\" : \"currency\", \"title\" : \"title\", \"endsAt\" : \"2000-01-23T04:56:07.000+00:00\", \"status\" : \"OPEN\" }, { \"auctionId\" : \"046b6c7f-0b8a-43b9-b35d-6489e6daee91\", \"sellerId\" : \"sellerId\", \"leadingBidderId\" : \"leadingBidderId\", \"currentPrice\" : \"currentPrice\", \"currency\" : \"currency\", \"title\" : \"title\", \"endsAt\" : \"2000-01-23T04:56:07.000+00:00\", \"status\" : \"OPEN\" } ] }";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
            }
        });
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }

    /**
     * GET /api/me/bids : Browse auctions where current user placed bids
     *
     * @param status  (optional)
     * @param sort  (optional)
     * @param limit  (optional, default to 20)
     * @param after  (optional)
     * @return Current user bid auctions (status code 200)
     *         or Authentication required (status code 401)
     * @see AuctionsApi#browseMyBids
     */
    default ResponseEntity<AuctionListResponse> browseMyBids(AuctionStatus status,
        AuctionSort sort,
        Integer limit,
        String after) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType: MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString = "{ \"nextCursor\" : \"nextCursor\", \"items\" : [ { \"auctionId\" : \"046b6c7f-0b8a-43b9-b35d-6489e6daee91\", \"sellerId\" : \"sellerId\", \"leadingBidderId\" : \"leadingBidderId\", \"currentPrice\" : \"currentPrice\", \"currency\" : \"currency\", \"title\" : \"title\", \"endsAt\" : \"2000-01-23T04:56:07.000+00:00\", \"status\" : \"OPEN\" }, { \"auctionId\" : \"046b6c7f-0b8a-43b9-b35d-6489e6daee91\", \"sellerId\" : \"sellerId\", \"leadingBidderId\" : \"leadingBidderId\", \"currentPrice\" : \"currentPrice\", \"currency\" : \"currency\", \"title\" : \"title\", \"endsAt\" : \"2000-01-23T04:56:07.000+00:00\", \"status\" : \"OPEN\" } ] }";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
            }
        });
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }

    /**
     * POST /api/auctions : Create a new auction
     *
     * @param createAuctionRequest  (required)
     * @return Auction created (status code 201)
     * @see AuctionsApi#createAuction
     */
    default ResponseEntity<AuctionResponse> createAuction(CreateAuctionRequest createAuctionRequest) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType: MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString = "{ \"auctionId\" : \"046b6c7f-0b8a-43b9-b35d-6489e6daee91\", \"sellerId\" : \"sellerId\", \"leadingBidderId\" : \"leadingBidderId\", \"bids\" : [ { \"amount\" : \"amount\", \"bidderId\" : \"bidderId\", \"currency\" : \"currency\", \"placedAt\" : \"2000-01-23T04:56:07.000+00:00\" }, { \"amount\" : \"amount\", \"bidderId\" : \"bidderId\", \"currency\" : \"currency\", \"placedAt\" : \"2000-01-23T04:56:07.000+00:00\" } ], \"currentPrice\" : \"currentPrice\", \"currency\" : \"currency\", \"title\" : \"title\", \"endsAt\" : \"2000-01-23T04:56:07.000+00:00\", \"status\" : \"OPEN\" }";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
            }
        });
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }

    /**
     * GET /api/auctions/{auctionId} : Get auction details
     *
     * @param auctionId  (required)
     * @return Auction details (status code 200)
     *         or Auction not found (status code 404)
     * @see AuctionsApi#getAuctionById
     */
    default ResponseEntity<AuctionResponse> getAuctionById(UUID auctionId) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType: MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString = "{ \"auctionId\" : \"046b6c7f-0b8a-43b9-b35d-6489e6daee91\", \"sellerId\" : \"sellerId\", \"leadingBidderId\" : \"leadingBidderId\", \"bids\" : [ { \"amount\" : \"amount\", \"bidderId\" : \"bidderId\", \"currency\" : \"currency\", \"placedAt\" : \"2000-01-23T04:56:07.000+00:00\" }, { \"amount\" : \"amount\", \"bidderId\" : \"bidderId\", \"currency\" : \"currency\", \"placedAt\" : \"2000-01-23T04:56:07.000+00:00\" } ], \"currentPrice\" : \"currentPrice\", \"currency\" : \"currency\", \"title\" : \"title\", \"endsAt\" : \"2000-01-23T04:56:07.000+00:00\", \"status\" : \"OPEN\" }";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
            }
        });
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }

    /**
     * GET /api/me : Get current authenticated user profile
     *
     * @return Current user profile (status code 200)
     *         or Authentication required (status code 401)
     * @see AuctionsApi#getCurrentUserProfile
     */
    default ResponseEntity<CurrentUserProfileResponse> getCurrentUserProfile() {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType: MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString = "{ \"provider\" : \"provider\", \"displayName\" : \"displayName\", \"verified\" : true, \"partyId\" : \"partyId\", \"email\" : \"email\" }";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
            }
        });
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }

}
