package com.stockyard.sale;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public final class SaleDtos {
    private SaleDtos() {}

    /** unitPrice defaults to the product's retail price; soldAt defaults to now. */
    public record SaleRequest(
            @NotNull Long productId,
            @NotNull Long channelId,
            @NotNull @Min(1) Integer quantity,
            @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal unitPrice,
            @PastOrPresent OffsetDateTime soldAt) {}

    public record SaleResponse(Long id, Long productId, String sku, String productTitle, Long channelId,
                               String channelName, int quantity, BigDecimal unitPrice, BigDecimal unitCost,
                               BigDecimal revenue, BigDecimal profit, OffsetDateTime soldAt) {
        public static SaleResponse of(Sale s) {
            BigDecimal qty = BigDecimal.valueOf(s.getQuantity());
            BigDecimal revenue = s.getUnitPrice().multiply(qty);
            return new SaleResponse(s.getId(), s.getProduct().getId(), s.getProduct().getSku(),
                    s.getProduct().getTitle(), s.getChannel().getId(), s.getChannel().getName(), s.getQuantity(),
                    s.getUnitPrice(), s.getUnitCost(), revenue, revenue.subtract(s.getUnitCost().multiply(qty)),
                    s.getSoldAt());
        }
    }
}
