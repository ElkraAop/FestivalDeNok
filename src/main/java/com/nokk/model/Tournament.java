package com.nokk.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Représente un des 3 arbres du tournoi : Or (level=1), Argent (level=2), Bronze (level=3).
 */
@Entity
@Table(name = "tournaments")
public class Tournament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;  // "Or", "Argent", "Bronze"

    @Min(1)
    @Column(nullable = false)
    private int level;    // 1=Or, 2=Argent, 3=Bronze

    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("roundIndex ASC")
    private Set<TournamentRound> rounds = new LinkedHashSet<>();

    // ── Constructeurs ──────────────────────────────────────────────────────
    public Tournament() {}

    public Tournament(String name, int level) {
        this.name  = name;
        this.level = level;
    }

    // ── Helper ─────────────────────────────────────────────────────────────
    public void addRound(TournamentRound round) {
        round.setTournament(this);
        this.rounds.add(round);
    }

    // ── Getters / Setters ──────────────────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public Set<TournamentRound> getRounds() { return rounds; }
    public void setRounds(Set<TournamentRound> rounds) { this.rounds = rounds; }
}
