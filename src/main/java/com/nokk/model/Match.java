package com.nokk.model;

import jakarta.persistence.*;

/**
 * Un match dans un tour.
 *
 * teamA = gagnant interne (vient du tour précédent du même arbre)
 * teamB = soit un autre gagnant interne (tour interne), soit un perdant qui descend (tour drop)
 *
 * teamAOrigin / teamBOrigin = labels affichés quand le slot est vide
 *   ex: "← Gagnant T1", "↓ Or T2"
 *
 * loserDropped = true si le perdant a déjà été placé dans le bracket inférieur
 *   (évite la double descente en cas de re-modification)
 */
@Entity
@Table(name = "matches",
       uniqueConstraints = @UniqueConstraint(columnNames = {"round_id", "match_index"}))
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id", nullable = false)
    private TournamentRound round;

    @Column(name = "match_index", nullable = false)
    private int matchIndex;

    // ── Joueurs ───────────────────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_a_id")
    private Player teamA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_b_id")
    private Player teamB;

    // ── Scores ────────────────────────────────────────────────────────────
    @Column(nullable = false)
    private int scoreA = 0;

    @Column(nullable = false)
    private int scoreB = 0;

    // ── État ─────────────────────────────────────────────────────────────
    @Column(nullable = false)
    private boolean finished = false;

    @Column(nullable = false)
    private boolean bye = false;

    @Column(nullable = false)
    private boolean loserDropped = false;

    // ── Labels TBD (affichage quand slot vide) ────────────────────────────
    @Column(name = "team_a_origin")
    private String teamAOrigin;

    @Column(name = "team_b_origin")
    private String teamBOrigin;

    // ── Constructeurs ──────────────────────────────────────────────────────
    public Match() {}

    public Match(int matchIndex) {
        this.matchIndex = matchIndex;
    }

    public Match(int matchIndex, String teamAOrigin, String teamBOrigin) {
        this.matchIndex   = matchIndex;
        this.teamAOrigin  = teamAOrigin;
        this.teamBOrigin  = teamBOrigin;
    }

    // ── Helpers ────────────────────────────────────────────────────────────
    public Player getWinner() {
        if (bye) return teamA;
        if (!finished || teamA == null || teamB == null) return null;
        if (scoreA > scoreB) return teamA;
        if (scoreB > scoreA) return teamB;
        return null;
    }

    public Player getLoser() {
        if (bye || !finished || teamA == null || teamB == null) return null;
        if (scoreA > scoreB) return teamB;
        if (scoreB > scoreA) return teamA;
        return null;
    }

    // ── Getters / Setters ──────────────────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TournamentRound getRound() { return round; }
    public void setRound(TournamentRound round) { this.round = round; }

    public int getMatchIndex() { return matchIndex; }
    public void setMatchIndex(int matchIndex) { this.matchIndex = matchIndex; }

    public Player getTeamA() { return teamA; }
    public void setTeamA(Player teamA) { this.teamA = teamA; }

    public Player getTeamB() { return teamB; }
    public void setTeamB(Player teamB) { this.teamB = teamB; }

    public int getScoreA() { return scoreA; }
    public void setScoreA(int scoreA) { this.scoreA = scoreA; }

    public int getScoreB() { return scoreB; }
    public void setScoreB(int scoreB) { this.scoreB = scoreB; }

    public boolean isFinished() { return finished; }
    public void setFinished(boolean finished) { this.finished = finished; }

    public boolean isBye() { return bye; }
    public void setBye(boolean bye) { this.bye = bye; }

    public boolean isLoserDropped() { return loserDropped; }
    public void setLoserDropped(boolean loserDropped) { this.loserDropped = loserDropped; }

    public String getTeamAOrigin() { return teamAOrigin; }
    public void setTeamAOrigin(String teamAOrigin) { this.teamAOrigin = teamAOrigin; }

    public String getTeamBOrigin() { return teamBOrigin; }
    public void setTeamBOrigin(String teamBOrigin) { this.teamBOrigin = teamBOrigin; }
}
