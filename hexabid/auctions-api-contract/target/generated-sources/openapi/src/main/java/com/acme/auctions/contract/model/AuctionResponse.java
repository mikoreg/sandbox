package com.acme.auctions.contract.model;

import java.net.URI;
import java.util.Objects;
import com.acme.auctions.contract.model.AuctionStatus;
import com.acme.auctions.contract.model.BidResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.Nullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * AuctionResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-15T22:32:57.773440636+01:00[Europe/Warsaw]", comments = "Generator version: 7.14.0")
public class AuctionResponse {

  private UUID auctionId;

  private String sellerId;

  private String title;

  private String currentPrice;

  private String currency;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime endsAt;

  private AuctionStatus status;

  private @Nullable String leadingBidderId;

  @Valid
  private List<@Valid BidResponse> bids = new ArrayList<>();

  public AuctionResponse() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public AuctionResponse(UUID auctionId, String sellerId, String title, String currentPrice, String currency, OffsetDateTime endsAt, AuctionStatus status, List<@Valid BidResponse> bids) {
    this.auctionId = auctionId;
    this.sellerId = sellerId;
    this.title = title;
    this.currentPrice = currentPrice;
    this.currency = currency;
    this.endsAt = endsAt;
    this.status = status;
    this.bids = bids;
  }

  public AuctionResponse auctionId(UUID auctionId) {
    this.auctionId = auctionId;
    return this;
  }

  /**
   * Get auctionId
   * @return auctionId
   */
  @NotNull @Valid 
  @JsonProperty("auctionId")
  public UUID getAuctionId() {
    return auctionId;
  }

  public void setAuctionId(UUID auctionId) {
    this.auctionId = auctionId;
  }

  public AuctionResponse sellerId(String sellerId) {
    this.sellerId = sellerId;
    return this;
  }

  /**
   * Get sellerId
   * @return sellerId
   */
  @NotNull 
  @JsonProperty("sellerId")
  public String getSellerId() {
    return sellerId;
  }

  public void setSellerId(String sellerId) {
    this.sellerId = sellerId;
  }

  public AuctionResponse title(String title) {
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

  public AuctionResponse currentPrice(String currentPrice) {
    this.currentPrice = currentPrice;
    return this;
  }

  /**
   * Get currentPrice
   * @return currentPrice
   */
  @NotNull 
  @JsonProperty("currentPrice")
  public String getCurrentPrice() {
    return currentPrice;
  }

  public void setCurrentPrice(String currentPrice) {
    this.currentPrice = currentPrice;
  }

  public AuctionResponse currency(String currency) {
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

  public AuctionResponse endsAt(OffsetDateTime endsAt) {
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

  public AuctionResponse status(AuctionStatus status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   * @return status
   */
  @NotNull @Valid 
  @JsonProperty("status")
  public AuctionStatus getStatus() {
    return status;
  }

  public void setStatus(AuctionStatus status) {
    this.status = status;
  }

  public AuctionResponse leadingBidderId(@Nullable String leadingBidderId) {
    this.leadingBidderId = leadingBidderId;
    return this;
  }

  /**
   * Get leadingBidderId
   * @return leadingBidderId
   */
  
  @JsonProperty("leadingBidderId")
  public @Nullable String getLeadingBidderId() {
    return leadingBidderId;
  }

  public void setLeadingBidderId(@Nullable String leadingBidderId) {
    this.leadingBidderId = leadingBidderId;
  }

  public AuctionResponse bids(List<@Valid BidResponse> bids) {
    this.bids = bids;
    return this;
  }

  public AuctionResponse addBidsItem(BidResponse bidsItem) {
    if (this.bids == null) {
      this.bids = new ArrayList<>();
    }
    this.bids.add(bidsItem);
    return this;
  }

  /**
   * Get bids
   * @return bids
   */
  @NotNull @Valid 
  @JsonProperty("bids")
  public List<@Valid BidResponse> getBids() {
    return bids;
  }

  public void setBids(List<@Valid BidResponse> bids) {
    this.bids = bids;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AuctionResponse auctionResponse = (AuctionResponse) o;
    return Objects.equals(this.auctionId, auctionResponse.auctionId) &&
        Objects.equals(this.sellerId, auctionResponse.sellerId) &&
        Objects.equals(this.title, auctionResponse.title) &&
        Objects.equals(this.currentPrice, auctionResponse.currentPrice) &&
        Objects.equals(this.currency, auctionResponse.currency) &&
        Objects.equals(this.endsAt, auctionResponse.endsAt) &&
        Objects.equals(this.status, auctionResponse.status) &&
        Objects.equals(this.leadingBidderId, auctionResponse.leadingBidderId) &&
        Objects.equals(this.bids, auctionResponse.bids);
  }

  @Override
  public int hashCode() {
    return Objects.hash(auctionId, sellerId, title, currentPrice, currency, endsAt, status, leadingBidderId, bids);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AuctionResponse {\n");
    sb.append("    auctionId: ").append(toIndentedString(auctionId)).append("\n");
    sb.append("    sellerId: ").append(toIndentedString(sellerId)).append("\n");
    sb.append("    title: ").append(toIndentedString(title)).append("\n");
    sb.append("    currentPrice: ").append(toIndentedString(currentPrice)).append("\n");
    sb.append("    currency: ").append(toIndentedString(currency)).append("\n");
    sb.append("    endsAt: ").append(toIndentedString(endsAt)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    leadingBidderId: ").append(toIndentedString(leadingBidderId)).append("\n");
    sb.append("    bids: ").append(toIndentedString(bids)).append("\n");
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

