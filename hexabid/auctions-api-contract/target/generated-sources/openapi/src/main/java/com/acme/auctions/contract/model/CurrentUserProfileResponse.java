package com.acme.auctions.contract.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.springframework.lang.Nullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * CurrentUserProfileResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-15T22:32:57.773440636+01:00[Europe/Warsaw]", comments = "Generator version: 7.14.0")
public class CurrentUserProfileResponse {

  private String partyId;

  private String provider;

  private String displayName;

  private @Nullable String email;

  private Boolean verified;

  public CurrentUserProfileResponse() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public CurrentUserProfileResponse(String partyId, String provider, String displayName, Boolean verified) {
    this.partyId = partyId;
    this.provider = provider;
    this.displayName = displayName;
    this.verified = verified;
  }

  public CurrentUserProfileResponse partyId(String partyId) {
    this.partyId = partyId;
    return this;
  }

  /**
   * Get partyId
   * @return partyId
   */
  @NotNull 
  @JsonProperty("partyId")
  public String getPartyId() {
    return partyId;
  }

  public void setPartyId(String partyId) {
    this.partyId = partyId;
  }

  public CurrentUserProfileResponse provider(String provider) {
    this.provider = provider;
    return this;
  }

  /**
   * Get provider
   * @return provider
   */
  @NotNull 
  @JsonProperty("provider")
  public String getProvider() {
    return provider;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }

  public CurrentUserProfileResponse displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  /**
   * Get displayName
   * @return displayName
   */
  @NotNull 
  @JsonProperty("displayName")
  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public CurrentUserProfileResponse email(@Nullable String email) {
    this.email = email;
    return this;
  }

  /**
   * Get email
   * @return email
   */
  
  @JsonProperty("email")
  public @Nullable String getEmail() {
    return email;
  }

  public void setEmail(@Nullable String email) {
    this.email = email;
  }

  public CurrentUserProfileResponse verified(Boolean verified) {
    this.verified = verified;
    return this;
  }

  /**
   * Get verified
   * @return verified
   */
  @NotNull 
  @JsonProperty("verified")
  public Boolean getVerified() {
    return verified;
  }

  public void setVerified(Boolean verified) {
    this.verified = verified;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CurrentUserProfileResponse currentUserProfileResponse = (CurrentUserProfileResponse) o;
    return Objects.equals(this.partyId, currentUserProfileResponse.partyId) &&
        Objects.equals(this.provider, currentUserProfileResponse.provider) &&
        Objects.equals(this.displayName, currentUserProfileResponse.displayName) &&
        Objects.equals(this.email, currentUserProfileResponse.email) &&
        Objects.equals(this.verified, currentUserProfileResponse.verified);
  }

  @Override
  public int hashCode() {
    return Objects.hash(partyId, provider, displayName, email, verified);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CurrentUserProfileResponse {\n");
    sb.append("    partyId: ").append(toIndentedString(partyId)).append("\n");
    sb.append("    provider: ").append(toIndentedString(provider)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
    sb.append("    verified: ").append(toIndentedString(verified)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

