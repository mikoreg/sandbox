package com.acme.auctions.contract.model;

import java.net.URI;
import java.util.Objects;
import com.acme.auctions.contract.model.AuctionStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.Nullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * AuctionListItemResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-15T22:32:57.773440636+01:00[Europe/Warsaw]", comments = "Generator version: 7.14.0")
public class AuctionListItemResponse {

  private UUID auctionId;

  private String sellerId;

  private String title;

  private String currentPrice;

  private String currency;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime endsAt;

  private AuctionStatus status;

  private @Nullable String leadingBidderId;

  public AuctionListItemResponse() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public AuctionListItemResponse(UUID auctionId, String sellerId, String title, String currentPrice, String currency, OffsetDateTime endsAt, AuctionStatus status) {
    this.auctionId = auctionId;
    this.sellerId = sellerId;
    this.title = title;
    this.currentPrice = currentPrice;
    this.currency = currency;
    this.endsAt = endsAt;
    this.status = status;
  }

  public AuctionListItemResponse auctionId(UUID auctionId) {
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

  public AuctionListItemResponse sellerId(String sellerId) {
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

  public AuctionListItemResponse title(String title) {
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

  public AuctionListItemResponse currentPrice(String currentPrice) {
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

  public AuctionListItemResponse currency(String currency) {
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

  public AuctionListItemResponse endsAt(OffsetDateTime endsAt) {
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

  public AuctionListItemResponse status(AuctionStatus status) {
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

  public AuctionListItemResponse leadingBidderId(@Nullable String leadingBidderId) {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AuctionListItemResponse auctionListItemResponse = (AuctionListItemResponse) o;
    return Objects.equals(this.auctionId, auctionListItemResponse.auctionId) &&
        Objects.equals(this.sellerId, auctionListItemResponse.sellerId) &&
        Objects.equals(this.title, auctionListItemResponse.title) &&
        Objects.equals(this.currentPrice, auctionListItemResponse.currentPrice) &&
        Objects.equals(this.currency, auctionListItemResponse.currency) &&
        Objects.equals(this.endsAt, auctionListItemResponse.endsAt) &&
        Objects.equals(this.status, auctionListItemResponse.status) &&
        Objects.equals(this.leadingBidderId, auctionListItemResponse.leadingBidderId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(auctionId, sellerId, title, currentPrice, currency, endsAt, status, leadingBidderId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AuctionListItemResponse {\n");
    sb.append("    auctionId: ").append(toIndentedString(auctionId)).append("\n");
    sb.append("    sellerId: ").append(toIndentedString(sellerId)).append("\n");
    sb.append("    title: ").append(toIndentedString(title)).append("\n");
    sb.append("    currentPrice: ").append(toIndentedString(currentPrice)).append("\n");
    sb.append("    currency: ").append(toIndentedString(currency)).append("\n");
    sb.append("    endsAt: ").append(toIndentedString(endsAt)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    leadingBidderId: ").append(toIndentedString(leadingBidderId)).append("\n");
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

