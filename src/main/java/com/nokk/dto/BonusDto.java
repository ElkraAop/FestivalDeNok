package com.nokk.dto;

import com.nokk.model.enums.CardSuit;
import com.nokk.model.enums.Weapon;
import jakarta.validation.constraints.NotNull;

// ═══════════════════════════════════════════════════════════════════════════
//  BonusDto — modification des bonus d'un joueur par l'admin
// ═══════════════════════════════════════════════════════════════════════════
public class BonusDto {

    @NotNull
    private Long playerId;

    // ── Modifications (null = pas modifié) ────────────────────────────────
    private Integer livesDelta;      // +1, -2, etc.
    private Integer doubleHitsDelta;
    private Weapon  weapon;

    // ── Carte ────────────────────────────────────────────────────────────
    private CardSuit cardSuit;
    private String   cardValue;

    // ── Note optionnelle pour l'historique ───────────────────────────────
    private String note;

    // ── Reset complet ─────────────────────────────────────────────────────
    private boolean reset = false;

    // ── Getters / Setters ──────────────────────────────────────────────────
    public Long getPlayerId() { return playerId; }
    public void setPlayerId(Long playerId) { this.playerId = playerId; }

    public Integer getLivesDelta() { return livesDelta; }
    public void setLivesDelta(Integer livesDelta) { this.livesDelta = livesDelta; }

    public Integer getDoubleHitsDelta() { return doubleHitsDelta; }
    public void setDoubleHitsDelta(Integer doubleHitsDelta) { this.doubleHitsDelta = doubleHitsDelta; }

    public Weapon getWeapon() { return weapon; }
    public void setWeapon(Weapon weapon) { this.weapon = weapon; }

    public CardSuit getCardSuit() { return cardSuit; }
    public void setCardSuit(CardSuit cardSuit) { this.cardSuit = cardSuit; }

    public String getCardValue() { return cardValue; }
    public void setCardValue(String cardValue) { this.cardValue = cardValue; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public boolean isReset() { return reset; }
    public void setReset(boolean reset) { this.reset = reset; }
}
