package com.stockyard.sale;

import com.stockyard.auth.MakerRepository;
import com.stockyard.channel.Channel;
import com.stockyard.channel.ChannelRepository;
import com.stockyard.common.ApiException;
import com.stockyard.product.Product;
import com.stockyard.product.ProductRepository;
import com.stockyard.sale.SaleDtos.*;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
public class SaleService {
    private static final int MAX_RESULTS = 500;

    private final SaleRepository sales;
    private final ProductRepository products;
    private final ChannelRepository channels;
    private final MakerRepository makers;

    public SaleService(SaleRepository sales, ProductRepository products, ChannelRepository channels,
                       MakerRepository makers) {
        this.sales = sales;
        this.products = products;
        this.channels = channels;
        this.makers = makers;
    }

    /** Stock decrement and ledger insert share one transaction: both commit or neither does. */
    @Transactional
    public SaleResponse record(Long makerId, SaleRequest req) {
        Product product = products.findByIdAndMakerId(req.productId(), makerId)
                .orElseThrow(() -> ApiException.notFound("Product"));
        if (product.isArchived()) throw ApiException.badRequest("Archived products cannot be sold");
        Channel channel = channels.findByIdAndMakerId(req.channelId(), makerId)
                .orElseThrow(() -> ApiException.notFound("Channel"));

        if (products.decrementStock(product.getId(), makerId, req.quantity()) == 0) {
            throw ApiException.conflict("Not enough stock for " + product.getSku());
        }
        // decrementStock clears the persistence context, so re-attach references.
        Product fresh = products.findById(product.getId()).orElseThrow();
        Sale sale = new Sale(makers.getReferenceById(makerId), fresh, channels.getReferenceById(channel.getId()),
                req.quantity(), req.unitPrice() != null ? req.unitPrice() : fresh.getRetailPrice());
        if (req.soldAt() != null) sale.setSoldAt(req.soldAt());
        return SaleResponse.of(sales.save(sale));
    }

    @Transactional(readOnly = true)
    public List<SaleResponse> list(Long makerId, LocalDate from, LocalDate to, Long channelId) {
        Specification<Sale> spec = (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            ps.add(cb.equal(root.get("maker").get("id"), makerId));
            if (from != null) {
                ps.add(cb.greaterThanOrEqualTo(root.get("soldAt"), from.atStartOfDay().atOffset(ZoneOffset.UTC)));
            }
            if (to != null) {
                ps.add(cb.lessThan(root.get("soldAt"), to.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC)));
            }
            if (channelId != null) ps.add(cb.equal(root.get("channel").get("id"), channelId));
            return cb.and(ps.toArray(Predicate[]::new));
        };
        return sales.findAll(spec, PageRequest.of(0, MAX_RESULTS, Sort.by(Sort.Direction.DESC, "soldAt")))
                .map(SaleResponse::of).getContent();
    }
}
