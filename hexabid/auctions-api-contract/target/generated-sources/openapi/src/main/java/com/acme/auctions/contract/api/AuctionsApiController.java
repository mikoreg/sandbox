package com.acme.auctions.contract.api;

import com.acme.auctions.contract.model.AuctionListResponse;
import com.acme.auctions.contract.model.AuctionResponse;
import com.acme.auctions.contract.model.AuctionSort;
import com.acme.auctions.contract.model.AuctionStatus;
import com.acme.auctions.contract.model.CreateAuctionRequest;
import com.acme.auctions.contract.model.CurrentUserProfileResponse;
import java.util.UUID;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import jakarta.annotation.Generated;

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-07T00:19:55.094556016+01:00[Europe/Warsaw]", comments = "Generator version: 7.14.0")
@Controller
@RequestMapping("${openapi.auctions.base-path:}")
public class AuctionsApiController implements AuctionsApi {

    private final AuctionsApiDelegate delegate;

    public AuctionsApiController(@Autowired(required = false) AuctionsApiDelegate delegate) {
        this.delegate = Optional.ofNullable(delegate).orElse(new AuctionsApiDelegate() {});
    }

    @Override
    public AuctionsApiDelegate getDelegate() {
        return delegate;
    }

}
