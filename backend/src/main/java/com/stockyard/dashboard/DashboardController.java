package com.stockyard.dashboard;

import com.stockyard.dashboard.DashboardDtos.*;
import com.stockyard.product.ProductDtos.ProductResponse;
import com.stockyard.product.ProductRepository;
import com.stockyard.sale.SaleRepository;
import com.stockyard.security.MakerPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final SaleRepository sales;
    private final ProductRepository products;

    public DashboardController(SaleRepository sales, ProductRepository products) {
        this.sales = sales;
        this.products = products;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public DashboardResponse get(@AuthenticationPrincipal MakerPrincipal me) {
        YearMonth month = YearMonth.now(ZoneOffset.UTC);
        OffsetDateTime from = month.atDay(1).atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime to = month.plusMonths(1).atDay(1).atStartOfDay().atOffset(ZoneOffset.UTC);
        Totals totals = sales.totals(me.id(), from, to);
        return new DashboardResponse(month.toString(), totals.revenue(), totals.units(), totals.profit(),
                sales.byChannel(me.id(), from, to),
                products.findLowStock(me.id()).stream().map(ProductResponse::of).toList());
    }
}
