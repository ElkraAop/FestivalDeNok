package com.nokk.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bonus_logs")
public class BonusLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // ── Constructeurs ──────────────────────────────────────────────────────
    public BonusLog() {}

    public BonusLog(Player player, String description) {
        this.player      = player;
        this.description = description;
        this.createdAt   = LocalDateTime.now();
    }

    // ── Getters / Setters ──────────────────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
