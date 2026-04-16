package com.nokk.dto;

import java.util.List;

// ═══════════════════════════════════════════════════════════════════════════
//  TournamentDto — bracket complet sérialisé pour le front
// ═══════════════════════════════════════════════════════════════════════════
public class TournamentDto {

    private Long              id;
    private String            name;
    private int               level;
    private List<RoundDto>    rounds;

    public TournamentDto() {}

    // ── Getters / Setters ──────────────────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public List<RoundDto> getRounds() { return rounds; }
    public void setRounds(List<RoundDto> rounds) { this.rounds = rounds; }

    // ── RoundDto ───────────────────────────────────────────────────────────
    public static class RoundDto {
        private Long           id;
        private int            roundIndex;
        private boolean        dropRound;
        private List<MatchNodeDto> matches;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public int getRoundIndex() { return roundIndex; }
        public void setRoundIndex(int roundIndex) { this.roundIndex = roundIndex; }

        public boolean isDropRound() { return dropRound; }
        public void setDropRound(boolean dropRound) { this.dropRound = dropRound; }

        public List<MatchNodeDto> getMatches() { return matches; }
        public void setMatches(List<MatchNodeDto> matches) { this.matches = matches; }
    }

    // ── MatchNodeDto ───────────────────────────────────────────────────────
    public static class MatchNodeDto {
        private Long   id;
        private int    matchIndex;

        // On envoie l'id ET le nom pour que le front puisse afficher sans lookup
        private Long   teamAId;
        private String teamAName;
        private Long   teamBId;
        private String teamBName;

        private int     scoreA;
        private int     scoreB;
        private boolean finished;
        private boolean bye;
        private boolean loserDropped;
        private String  teamAOrigin;
        private String  teamBOrigin;

        // ── Inventaire joueurs ───────────────────────────────────────────
        private int    teamALives;
        private int    teamADoubleHits;
        private String teamAWeapon;
        private String teamACard;       // null si pas de carte

        private int    teamBLives;
        private int    teamBDoubleHits;
        private String teamBWeapon;
        private String teamBCard;

        // ── Getters / Setters ────────────────────────────────────────────
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public int getMatchIndex() { return matchIndex; }
        public void setMatchIndex(int matchIndex) { this.matchIndex = matchIndex; }

        public Long getTeamAId() { return teamAId; }
        public void setTeamAId(Long teamAId) { this.teamAId = teamAId; }

        public String getTeamAName() { return teamAName; }
        public void setTeamAName(String teamAName) { this.teamAName = teamAName; }

        public Long getTeamBId() { return teamBId; }
        public void setTeamBId(Long teamBId) { this.teamBId = teamBId; }

        public String getTeamBName() { return teamBName; }
        public void setTeamBName(String teamBName) { this.teamBName = teamBName; }

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

        public int getTeamALives() { return teamALives; }
        public void setTeamALives(int teamALives) { this.teamALives = teamALives; }

        public int getTeamADoubleHits() { return teamADoubleHits; }
        public void setTeamADoubleHits(int teamADoubleHits) { this.teamADoubleHits = teamADoubleHits; }

        public String getTeamAWeapon() { return teamAWeapon; }
        public void setTeamAWeapon(String teamAWeapon) { this.teamAWeapon = teamAWeapon; }

        public String getTeamACard() { return teamACard; }
        public void setTeamACard(String teamACard) { this.teamACard = teamACard; }

        public int getTeamBLives() { return teamBLives; }
        public void setTeamBLives(int teamBLives) { this.teamBLives = teamBLives; }

        public int getTeamBDoubleHits() { return teamBDoubleHits; }
        public void setTeamBDoubleHits(int teamBDoubleHits) { this.teamBDoubleHits = teamBDoubleHits; }

        public String getTeamBWeapon() { return teamBWeapon; }
        public void setTeamBWeapon(String teamBWeapon) { this.teamBWeapon = teamBWeapon; }

        public String getTeamBCard() { return teamBCard; }
        public void setTeamBCard(String teamBCard) { this.teamBCard = teamBCard; }
    }
}
