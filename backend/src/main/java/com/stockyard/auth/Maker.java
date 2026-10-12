package com.stockyard.auth;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "makers")
public class Maker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    @Column(name = "shop_name", nullable = false)
    private String shopName;
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    protected Maker() {}

    public Maker(String email, String passwordHash, String shopName) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.shopName = shopName;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getShopName() { return shopName; }
}
