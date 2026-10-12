package com.stockyard.product;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public final class ProductDtos {
    private ProductDtos() {}

    public record ProductRequest(
            @NotBlank @Size(max = 64) String sku,
            @NotBlank @Size(max = 200) String title,
            @Size(max = 500) String imageUrl,
            @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal unitCost,
            @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal retailPrice,
            @NotNull @Min(0) Integer stock,
            @Min(0) Integer lowStockThreshold) {}

    public record ProductResponse(Long id, String sku, String title, String imageUrl, BigDecimal unitCost,
                                  BigDecimal retailPrice, int stock, int lowStockThreshold, boolean archived,
                                  boolean lowStock) {
        public static ProductResponse of(Product p) {
            return new ProductResponse(p.getId(), p.getSku(), p.getTitle(), p.getImageUrl(), p.getUnitCost(),
                    p.getRetailPrice(), p.getStock(), p.getLowStockThreshold(), p.isArchived(),
                    p.getStock() <= p.getLowStockThreshold());
        }
    }
}
