package com.nokk.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

// ═══════════════════════════════════════════════════════════════════════════
//  MatchDto — résultat d'un match soumis par l'admin
// ═══════════════════════════════════════════════════════════════════════════
public class MatchDto {

    @NotNull
    private Long tournamentId;

    @NotNull
    @Min(0)
    private Integer roundIndex;

    @NotNull
    @Min(0)
    private Integer matchIndex;

    private Long teamAId;
    private Long teamBId;

    @Min(0)
    private int scoreA = 0;

    @Min(0)
    private int scoreB = 0;

    private boolean finished = false;

    // ── Getters / Setters ──────────────────────────────────────────────────
    public Long getTournamentId() { return tournamentId; }
    public void setTournamentId(Long tournamentId) { this.tournamentId = tournamentId; }

    public Integer getRoundIndex() { return roundIndex; }
    public void setRoundIndex(Integer roundIndex) { this.roundIndex = roundIndex; }

    public Integer getMatchIndex() { return matchIndex; }
    public void setMatchIndex(Integer matchIndex) { this.matchIndex = matchIndex; }

    public Long getTeamAId() { return teamAId; }
    public void setTeamAId(Long teamAId) { this.teamAId = teamAId; }

    public Long getTeamBId() { return teamBId; }
    public void setTeamBId(Long teamBId) { this.teamBId = teamBId; }

    public int getScoreA() { return scoreA; }
    public void setScoreA(int scoreA) { this.scoreA = scoreA; }

    public int getScoreB() { return scoreB; }
    public void setScoreB(int scoreB) { this.scoreB = scoreB; }

    public boolean isFinished() { return finished; }
    public void setFinished(boolean finished) { this.finished = finished; }
}
