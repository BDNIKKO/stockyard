package com.stockyard.sale;

import com.stockyard.auth.Maker;
import com.stockyard.channel.Channel;
import com.stockyard.product.Product;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "sales")
public class Sale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "maker_id")
    private Maker maker;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "channel_id")
    private Channel channel;
    @Column(nullable = false)
    private int quantity;
    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;
    @Column(name = "unit_cost", nullable = false)
    private BigDecimal unitCost;
    @Column(name = "sold_at", nullable = false)
    private OffsetDateTime soldAt = OffsetDateTime.now();

    protected Sale() {}

    public Sale(Maker maker, Product product, Channel channel, int quantity, BigDecimal unitPrice) {
        this.maker = maker;
        this.product = product;
        this.channel = channel;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.unitCost = product.getUnitCost();
    }

    public Long getId() { return id; }
    public Product getProduct() { return product; }
    public Channel getChannel() { return channel; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getUnitCost() { return unitCost; }
    public OffsetDateTime getSoldAt() { return soldAt; }
    public void setSoldAt(OffsetDateTime soldAt) { this.soldAt = soldAt; }
}
