package com.acme.auctions.contract.auth.model;

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
 * AuthProviderResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-15T22:32:58.360014502+01:00[Europe/Warsaw]", comments = "Generator version: 7.14.0")
public class AuthProviderResponse {

  private String registrationId;

  private String name;

  private String loginUrl;

  public AuthProviderResponse() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public AuthProviderResponse(String registrationId, String name, String loginUrl) {
    this.registrationId = registrationId;
    this.name = name;
    this.loginUrl = loginUrl;
  }

  public AuthProviderResponse registrationId(String registrationId) {
    this.registrationId = registrationId;
    return this;
  }

  /**
   * Unique identifier of the provider (e.g., 'google', 'local')
   * @return registrationId
   */
  @NotNull 
  @JsonProperty("registrationId")
  public String getRegistrationId() {
    return registrationId;
  }

  public void setRegistrationId(String registrationId) {
    this.registrationId = registrationId;
  }

  public AuthProviderResponse name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Human-readable name of the provider
   * @return name
   */
  @NotNull 
  @JsonProperty("name")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public AuthProviderResponse loginUrl(String loginUrl) {
    this.loginUrl = loginUrl;
    return this;
  }

  /**
   * Relative URL to initiate the login process
   * @return loginUrl
   */
  @NotNull 
  @JsonProperty("loginUrl")
  public String getLoginUrl() {
    return loginUrl;
  }

  public void setLoginUrl(String loginUrl) {
    this.loginUrl = loginUrl;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AuthProviderResponse authProviderResponse = (AuthProviderResponse) o;
    return Objects.equals(this.registrationId, authProviderResponse.registrationId) &&
        Objects.equals(this.name, authProviderResponse.name) &&
        Objects.equals(this.loginUrl, authProviderResponse.loginUrl);
  }

  @Override
  public int hashCode() {
    return Objects.hash(registrationId, name, loginUrl);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AuthProviderResponse {\n");
    sb.append("    registrationId: ").append(toIndentedString(registrationId)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    loginUrl: ").append(toIndentedString(loginUrl)).append("\n");
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

