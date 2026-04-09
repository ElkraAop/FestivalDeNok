package com.nokk.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Un tour dans un arbre.
 * roundIndex = 0 pour T1, 1 pour T2, etc.
 * isDropRound = true si ce tour reçoit des perdants du bracket supérieur (tours pairs Argent/Bronze).
 */
@Entity
@Table(name = "tournament_rounds",
       uniqueConstraints = @UniqueConstraint(columnNames = {"tournament_id", "round_index"}))
public class TournamentRound {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @Column(name = "round_index", nullable = false)
    private int roundIndex;

    /**
     * true  = tour "avec descente" (1 prédécesseur + 1 perdant du bracket sup)
     * false = tour "interne"       (2 prédécesseurs dans le même arbre)
     * Toujours false pour le bracket Or.
     */
    @Column(nullable = false)
    private boolean dropRound = false;

    @OneToMany(mappedBy = "round", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("matchIndex ASC")
    private Set<Match> matches = new LinkedHashSet<>();
    

    // ── Constructeurs ──────────────────────────────────────────────────────
    public TournamentRound() {}

    public TournamentRound(int roundIndex, boolean dropRound) {
        this.roundIndex = roundIndex;
        this.dropRound  = dropRound;
    }

    // ── Helper ─────────────────────────────────────────────────────────────
    public void addMatch(Match match) {
        match.setRound(this);
        this.matches.add(match);
    }

    // ── Getters / Setters ──────────────────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Tournament getTournament() { return tournament; }
    public void setTournament(Tournament tournament) { this.tournament = tournament; }

    public int getRoundIndex() { return roundIndex; }
    public void setRoundIndex(int roundIndex) { this.roundIndex = roundIndex; }

    public boolean isDropRound() { return dropRound; }
    public void setDropRound(boolean dropRound) { this.dropRound = dropRound; }

    public Set<Match> getMatches() { return matches; }
    public void setMatches(Set<Match> matches) { this.matches = matches; }
}
