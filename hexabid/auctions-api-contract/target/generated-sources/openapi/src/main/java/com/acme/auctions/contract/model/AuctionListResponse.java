package com.acme.auctions.contract.model;

import java.net.URI;
import java.util.Objects;
import com.acme.auctions.contract.model.AuctionListItemResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.lang.Nullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * AuctionListResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-15T22:32:57.773440636+01:00[Europe/Warsaw]", comments = "Generator version: 7.14.0")
public class AuctionListResponse {

  @Valid
  private List<@Valid AuctionListItemResponse> items = new ArrayList<>();

  private @Nullable String nextCursor;

  public AuctionListResponse() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public AuctionListResponse(List<@Valid AuctionListItemResponse> items) {
    this.items = items;
  }

  public AuctionListResponse items(List<@Valid AuctionListItemResponse> items) {
    this.items = items;
    return this;
  }

  public AuctionListResponse addItemsItem(AuctionListItemResponse itemsItem) {
    if (this.items == null) {
      this.items = new ArrayList<>();
    }
    this.items.add(itemsItem);
    return this;
  }

  /**
   * Get items
   * @return items
   */
  @NotNull @Valid 
  @JsonProperty("items")
  public List<@Valid AuctionListItemResponse> getItems() {
    return items;
  }

  public void setItems(List<@Valid AuctionListItemResponse> items) {
    this.items = items;
  }

  public AuctionListResponse nextCursor(@Nullable String nextCursor) {
    this.nextCursor = nextCursor;
    return this;
  }

  /**
   * Get nextCursor
   * @return nextCursor
   */
  
  @JsonProperty("nextCursor")
  public @Nullable String getNextCursor() {
    return nextCursor;
  }

  public void setNextCursor(@Nullable String nextCursor) {
    this.nextCursor = nextCursor;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AuctionListResponse auctionListResponse = (AuctionListResponse) o;
    return Objects.equals(this.items, auctionListResponse.items) &&
        Objects.equals(this.nextCursor, auctionListResponse.nextCursor);
  }

  @Override
  public int hashCode() {
    return Objects.hash(items, nextCursor);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AuctionListResponse {\n");
    sb.append("    items: ").append(toIndentedString(items)).append("\n");
    sb.append("    nextCursor: ").append(toIndentedString(nextCursor)).append("\n");
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

