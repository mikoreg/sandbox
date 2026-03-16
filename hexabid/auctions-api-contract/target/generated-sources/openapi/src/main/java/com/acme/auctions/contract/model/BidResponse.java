package com.acme.auctions.contract.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.time.OffsetDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.Nullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * BidResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-15T22:32:57.773440636+01:00[Europe/Warsaw]", comments = "Generator version: 7.14.0")
public class BidResponse {

  private String bidderId;

  private String amount;

  private String currency;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime placedAt;

  public BidResponse() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public BidResponse(String bidderId, String amount, String currency, OffsetDateTime placedAt) {
    this.bidderId = bidderId;
    this.amount = amount;
    this.currency = currency;
    this.placedAt = placedAt;
  }

  public BidResponse bidderId(String bidderId) {
    this.bidderId = bidderId;
    return this;
  }

  /**
   * Get bidderId
   * @return bidderId
   */
  @NotNull 
  @JsonProperty("bidderId")
  public String getBidderId() {
    return bidderId;
  }

  public void setBidderId(String bidderId) {
    this.bidderId = bidderId;
  }

  public BidResponse amount(String amount) {
    this.amount = amount;
    return this;
  }

  /**
   * Get amount
   * @return amount
   */
  @NotNull 
  @JsonProperty("amount")
  public String getAmount() {
    return amount;
  }

  public void setAmount(String amount) {
    this.amount = amount;
  }

  public BidResponse currency(String currency) {
    this.currency = currency;
    return this;
  }

  /**
   * Get currency
   * @return currency
   */
  @NotNull 
  @JsonProperty("currency")
  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public BidResponse placedAt(OffsetDateTime placedAt) {
    this.placedAt = placedAt;
    return this;
  }

  /**
   * Get placedAt
   * @return placedAt
   */
  @NotNull @Valid 
  @JsonProperty("placedAt")
  public OffsetDateTime getPlacedAt() {
    return placedAt;
  }

  public void setPlacedAt(OffsetDateTime placedAt) {
    this.placedAt = placedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BidResponse bidResponse = (BidResponse) o;
    return Objects.equals(this.bidderId, bidResponse.bidderId) &&
        Objects.equals(this.amount, bidResponse.amount) &&
        Objects.equals(this.currency, bidResponse.currency) &&
        Objects.equals(this.placedAt, bidResponse.placedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(bidderId, amount, currency, placedAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BidResponse {\n");
    sb.append("    bidderId: ").append(toIndentedString(bidderId)).append("\n");
    sb.append("    amount: ").append(toIndentedString(amount)).append("\n");
    sb.append("    currency: ").append(toIndentedString(currency)).append("\n");
    sb.append("    placedAt: ").append(toIndentedString(placedAt)).append("\n");
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

