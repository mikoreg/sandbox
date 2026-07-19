package com.acme.auctions.core.lot.model;

import com.acme.auctions.core.product.model.Product;

import java.util.Objects;

public record Lot(String title, Product product) {

    public Lot {
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(product, "product must not be null");
        if (title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
    }

    public static Lot singleProductDraft(String title) {
        return new Lot(title, Product.draft(title));
    }
}
