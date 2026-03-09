package com.acme.auctions.core.product.model;

import java.util.Objects;
import java.util.UUID;

public record ProductId(UUID value) {

    public ProductId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static ProductId newId() {
        return new ProductId(UUID.randomUUID());
    }
}
