package com.nokk.model;

import com.nokk.model.enums.CardSuit;
import com.nokk.model.enums.Weapon;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private int lives = 3;

    @Column(nullable = false)
    private int doubleHits = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Weapon weapon = Weapon.SIMPLE;

    // ── Carte piochée (nullable = pas encore piochée) ─────────────────────
    @Enumerated(EnumType.STRING)
    @Column(name = "card_suit")
    private CardSuit cardSuit;

    @Column(name = "card_value")
    private String cardValue; // "A", "2"…"10", "J", "Q", "K"

    // ── Historique des bonus ───────────────────────────────────────────────
    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<BonusLog> bonusHistory = new ArrayList<>();

    // ── Constructeurs ──────────────────────────────────────────────────────
    public Player() {}

    public Player(String name) {
        this.name = name;
    }

    // ── Getters / Setters ──────────────────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getLives() { return lives; }
    public void setLives(int lives) { this.lives = Math.max(0, lives); }

    public int getDoubleHits() { return doubleHits; }
    public void setDoubleHits(int doubleHits) { this.doubleHits = Math.max(0, doubleHits); }

    public Weapon getWeapon() { return weapon; }
    public void setWeapon(Weapon weapon) { this.weapon = weapon; }

    public CardSuit getCardSuit() { return cardSuit; }
    public void setCardSuit(CardSuit cardSuit) { this.cardSuit = cardSuit; }

    public String getCardValue() { return cardValue; }
    public void setCardValue(String cardValue) { this.cardValue = cardValue; }

    public List<BonusLog> getBonusHistory() { return bonusHistory; }
    public void setBonusHistory(List<BonusLog> bonusHistory) { this.bonusHistory = bonusHistory; }

    // ── Helper ────────────────────────────────────────────────────────────
    public boolean hasCard() { return cardSuit != null && cardValue != null; }

    public void reset(String note) {
        this.lives      = 3;
        this.doubleHits = 0;
        this.weapon     = Weapon.SIMPLE;
        BonusLog log = new BonusLog();
        log.setPlayer(this);
        log.setDescription("🔄 Reset bonus" + (note != null && !note.isBlank() ? " — " + note : ""));
        this.bonusHistory.add(log);
    }
}
