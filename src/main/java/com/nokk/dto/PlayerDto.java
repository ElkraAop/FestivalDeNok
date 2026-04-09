package com.nokk.dto;

import com.nokk.model.enums.CardSuit;
import com.nokk.model.enums.Weapon;
import java.time.LocalDateTime;
import java.util.List;

// ═══════════════════════════════════════════════════════════════════════════
//  PlayerDto — données complètes du joueur
// ═══════════════════════════════════════════════════════════════════════════
public class PlayerDto {

    private Long              id;
    private String            name;
    private int               lives;
    private int               doubleHits;
    private Weapon            weapon;
    private CardSuit          cardSuit;
    private String            cardValue;
    private boolean           hasCard;
    private List<BonusLogDto> bonusHistory;

    public PlayerDto() {}

    // ── Getters / Setters ──────────────────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getLives() { return lives; }
    public void setLives(int lives) { this.lives = lives; }

    public int getDoubleHits() { return doubleHits; }
    public void setDoubleHits(int doubleHits) { this.doubleHits = doubleHits; }

    public Weapon getWeapon() { return weapon; }
    public void setWeapon(Weapon weapon) { this.weapon = weapon; }

    public CardSuit getCardSuit() { return cardSuit; }
    public void setCardSuit(CardSuit cardSuit) { this.cardSuit = cardSuit; }

    public String getCardValue() { return cardValue; }
    public void setCardValue(String cardValue) { this.cardValue = cardValue; }

    public boolean isHasCard() { return hasCard; }
    public void setHasCard(boolean hasCard) { this.hasCard = hasCard; }

    public List<BonusLogDto> getBonusHistory() { return bonusHistory; }
    public void setBonusHistory(List<BonusLogDto> bonusHistory) { this.bonusHistory = bonusHistory; }

    // ── BonusLogDto imbriqué ───────────────────────────────────────────────
    public static class BonusLogDto {
        private Long          id;
        private String        description;
        private LocalDateTime createdAt;

        public BonusLogDto() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
}
