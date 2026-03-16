package com.acme.auctions.contract.auth.api;

import com.acme.auctions.contract.auth.model.AuthProviderResponse;


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

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-15T21:57:22.975399073+01:00[Europe/Warsaw]", comments = "Generator version: 7.14.0")
@Controller
@RequestMapping("${openapi.hexabidAuthentication.base-path:}")
public class AuthApiController implements AuthApi {

    private final AuthApiDelegate delegate;

    public AuthApiController(@Autowired(required = false) AuthApiDelegate delegate) {
        this.delegate = Optional.ofNullable(delegate).orElse(new AuthApiDelegate() {});
    }

    @Override
    public AuthApiDelegate getDelegate() {
        return delegate;
    }

}
