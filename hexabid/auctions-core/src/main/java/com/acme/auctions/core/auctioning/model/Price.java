package com.acme.auctions.core.auctioning.model;

import com.acme.auctions.core.auctioning.exception.CurrencyMismatchException;

import java.math.BigDecimal;
import java.util.Objects;

public record Price(BigDecimal amount, String currency) {

    public Price {
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero");
        }
        if (currency.isBlank()) {
            throw new IllegalArgumentException("currency must not be blank");
        }
        currency = currency.toUpperCase();
    }

    public boolean isGreaterThan(Price other) {
        verifyCurrency(other);
        return amount.compareTo(other.amount) > 0;
    }

    public int compareTo(Price other) {
        verifyCurrency(other);
        return amount.compareTo(other.amount);
    }

    private void verifyCurrency(Price other) {
        Objects.requireNonNull(other, "other must not be null");
        if (!currency.equals(other.currency)) {
            throw new CurrencyMismatchException();
        }
    }
}
