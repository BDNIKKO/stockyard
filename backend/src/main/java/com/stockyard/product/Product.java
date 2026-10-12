package com.stockyard.product;

import com.stockyard.auth.Maker;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "maker_id")
    private Maker maker;
    @Column(nullable = false)
    private String sku;
    @Column(nullable = false)
    private String title;
    @Column(name = "image_url")
    private String imageUrl;
    @Column(name = "unit_cost", nullable = false)
    private BigDecimal unitCost;
    @Column(name = "retail_price", nullable = false)
    private BigDecimal retailPrice;
    @Column(nullable = false)
    private int stock;
    @Column(name = "low_stock_threshold", nullable = false)
    private int lowStockThreshold = 5;
    @Column(nullable = false)
    private boolean archived;
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    protected Product() {}

    public Product(Maker maker) { this.maker = maker; }

    public Long getId() { return id; }
    public Maker getMaker() { return maker; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }
    public BigDecimal getRetailPrice() { return retailPrice; }
    public void setRetailPrice(BigDecimal retailPrice) { this.retailPrice = retailPrice; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public int getLowStockThreshold() { return lowStockThreshold; }
    public void setLowStockThreshold(int t) { this.lowStockThreshold = t; }
    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }
}
