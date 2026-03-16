package com.acme.auctions.contract.auth.api;

import com.acme.auctions.contract.auth.model.AuthProviderResponse;
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
 * A delegate to be called by the {@link AuthApiController}}.
 * Implement this interface with a {@link org.springframework.stereotype.Service} annotated class.
 */
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-15T22:32:58.360014502+01:00[Europe/Warsaw]", comments = "Generator version: 7.14.0")
public interface AuthApiDelegate {

    default Optional<NativeWebRequest> getRequest() {
        return Optional.empty();
    }

    /**
     * GET /api/auth/providers : Get available authentication providers
     *
     * @return List of available providers (status code 200)
     * @see AuthApi#getAuthProviders
     */
    default ResponseEntity<List<AuthProviderResponse>> getAuthProviders() {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType: MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString = "[ { \"loginUrl\" : \"loginUrl\", \"registrationId\" : \"registrationId\", \"name\" : \"name\" }, { \"loginUrl\" : \"loginUrl\", \"registrationId\" : \"registrationId\", \"name\" : \"name\" } ]";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
            }
        });
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }

}
