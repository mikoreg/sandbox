package com.acme.auctions.contract.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonValue;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;


import java.util.*;
import jakarta.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets AuctionSort
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-07T00:19:55.094556016+01:00[Europe/Warsaw]", comments = "Generator version: 7.14.0")
public enum AuctionSort {
  
  ENDING_SOON("ENDING_SOON"),
  
  ENDING_LATEST("ENDING_LATEST");

  private final String value;

  AuctionSort(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static AuctionSort fromValue(String value) {
    for (AuctionSort b : AuctionSort.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}

