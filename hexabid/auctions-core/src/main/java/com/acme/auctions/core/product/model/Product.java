package com.acme.auctions.core.product.model;

import java.util.Objects;

public record Product(ProductId productId, String name) {

    public Product {
        Objects.requireNonNull(productId, "productId must not be null");
        Objects.requireNonNull(name, "name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
    }

    public static Product draft(String name) {
        return new Product(ProductId.newId(), name);
    }
}
