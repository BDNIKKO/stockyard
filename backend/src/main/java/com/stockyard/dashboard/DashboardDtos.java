package com.stockyard.dashboard;

import com.stockyard.product.ProductDtos.ProductResponse;

import java.math.BigDecimal;
import java.util.List;

public final class DashboardDtos {
    private DashboardDtos() {}

    /** JPQL constructor target; aggregates are null when there are no rows. */
    public record Totals(BigDecimal revenue, Long units, BigDecimal cost) {
        public Totals {
            revenue = revenue == null ? BigDecimal.ZERO : revenue;
            units = units == null ? 0L : units;
            cost = cost == null ? BigDecimal.ZERO : cost;
        }

        public BigDecimal profit() { return revenue.subtract(cost); }
    }

    public record ChannelStat(Long channelId, String channelName, BigDecimal revenue, Long units, BigDecimal cost) {
        public BigDecimal profit() { return revenue.subtract(cost); }
    }

    public record DashboardResponse(String month, BigDecimal revenue, long units, BigDecimal profit,
                                    List<ChannelStat> channels, List<ProductResponse> lowStock) {}
}
