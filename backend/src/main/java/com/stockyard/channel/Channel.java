package com.stockyard.channel;

import com.stockyard.auth.Maker;
import jakarta.persistence.*;

@Entity
@Table(name = "channels")
public class Channel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "maker_id")
    private Maker maker;
    @Column(nullable = false)
    private String name;

    protected Channel() {}

    public Channel(Maker maker, String name) {
        this.maker = maker;
        this.name = name;
    }

    public Long getId() { return id; }
    public Maker getMaker() { return maker; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
