package com.acme.auctions.core.auctioning.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents a monetary value (Value Object).
 */
public record Price(BigDecimal amount, String currency) {
    public Price {
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price amount cannot be negative.");
        }
        if (currency.isBlank()) {
            throw new IllegalArgumentException("Currency cannot be blank.");
        }
    }
}
