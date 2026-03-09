package com.acme.auctions.contract.model;

import java.net.URI;
import java.util.Objects;
import com.acme.auctions.contract.model.Money;
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
 * CreateAuctionRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-07T00:19:55.094556016+01:00[Europe/Warsaw]", comments = "Generator version: 7.14.0")
public class CreateAuctionRequest {

  private String title;

  private Money startingPrice;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime endsAt;

  public CreateAuctionRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public CreateAuctionRequest(String title, Money startingPrice, OffsetDateTime endsAt) {
    this.title = title;
    this.startingPrice = startingPrice;
    this.endsAt = endsAt;
  }

  public CreateAuctionRequest title(String title) {
    this.title = title;
    return this;
  }

  /**
   * Get title
   * @return title
   */
  @NotNull 
  @JsonProperty("title")
  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public CreateAuctionRequest startingPrice(Money startingPrice) {
    this.startingPrice = startingPrice;
    return this;
  }

  /**
   * Get startingPrice
   * @return startingPrice
   */
  @NotNull @Valid 
  @JsonProperty("startingPrice")
  public Money getStartingPrice() {
    return startingPrice;
  }

  public void setStartingPrice(Money startingPrice) {
    this.startingPrice = startingPrice;
  }

  public CreateAuctionRequest endsAt(OffsetDateTime endsAt) {
    this.endsAt = endsAt;
    return this;
  }

  /**
   * Get endsAt
   * @return endsAt
   */
  @NotNull @Valid 
  @JsonProperty("endsAt")
  public OffsetDateTime getEndsAt() {
    return endsAt;
  }

  public void setEndsAt(OffsetDateTime endsAt) {
    this.endsAt = endsAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreateAuctionRequest createAuctionRequest = (CreateAuctionRequest) o;
    return Objects.equals(this.title, createAuctionRequest.title) &&
        Objects.equals(this.startingPrice, createAuctionRequest.startingPrice) &&
        Objects.equals(this.endsAt, createAuctionRequest.endsAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(title, startingPrice, endsAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreateAuctionRequest {\n");
    sb.append("    title: ").append(toIndentedString(title)).append("\n");
    sb.append("    startingPrice: ").append(toIndentedString(startingPrice)).append("\n");
    sb.append("    endsAt: ").append(toIndentedString(endsAt)).append("\n");
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

