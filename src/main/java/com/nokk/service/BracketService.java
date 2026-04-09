package com.nokk.service;

import com.nokk.model.*;
import com.nokk.repository.MatchRepository;
import com.nokk.repository.TournamentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class BracketService {

    private final TournamentRepository tournamentRepository;
    private final MatchRepository      matchRepository;
    private final MatchService         matchService;

    public BracketService(TournamentRepository tournamentRepository,
                          MatchRepository matchRepository,
                          MatchService matchService) {
        this.tournamentRepository = tournamentRepository;
        this.matchRepository      = matchRepository;
        this.matchService         = matchService;
    }

    // ── Point d'entrée ────────────────────────────────────────────────────
    public List<Tournament> initializeTournament(List<Player> players) {
        List<List<Match>> orRoundMatches = buildOrRounds(players);
        Tournament or     = buildTournament("Or",     1, orRoundMatches, false);
        Tournament argent = buildTournament("Argent", 2,
                buildLoserRounds(orRoundMatches, "Or"), true);
        Tournament bronze = buildTournament("Bronze", 3,
                buildLoserRounds(extractRounds(argent), "Argent"), true);

        tournamentRepository.save(or);
        tournamentRepository.save(argent);
        tournamentRepository.save(bronze);

        // ✅ Auto-valider les byes sur les 3 brackets
        for (Tournament tournament : List.of(or, argent, bronze)) {
            for (TournamentRound round : tournament.getRounds()) {
                for (Match match : round.getMatches()) {
                    if (match.isBye() && match.getTeamA() != null) {
                        match.setScoreA(1);
                        match.setScoreB(0);
                        match.setFinished(true);
                        matchRepository.save(match);
                        matchService.propagateWinner(tournament,
                                round.getRoundIndex(),
                                match.getMatchIndex());
                    }
                }
            }
        }
        return List.of(or, argent, bronze);
    }

    // ── Bracket Or ────────────────────────────────────────────────────────
    private List<List<Match>> buildOrRounds(List<Player> players) {
        List<List<Match>> rounds = new ArrayList<>();

        // Tour 1 : appairage des joueurs
        List<Match> r1 = new ArrayList<>();
        for (int i = 0; i < players.size(); i += 2) {
            Match m = new Match(i / 2);
            m.setTeamA(players.get(i));
            if (i + 1 < players.size()) {
                m.setTeamB(players.get(i + 1));
            } else {
                m.setBye(true);
                m.setFinished(true);
            }
            r1.add(m);
        }
        rounds.add(r1);

        // Tours suivants : slots vides
        List<Match> prev = r1;
        while (prev.size() > 1) {
            int count = (int) Math.ceil(prev.size() / 2.0);
            List<Match> next = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                next.add(new Match(i));
            }
            rounds.add(next);
            prev = next;
        }

        return rounds;
    }

    // ── Bracket Argent / Bronze ───────────────────────────────────────────
    private List<List<Match>> buildLoserRounds(List<List<Match>> upperRounds,
                                               String upperName) {
        List<List<Match>> rounds = new ArrayList<>();

        List<Integer> upperLosers = new ArrayList<>();
        for (List<Match> r : upperRounds) {
            int losers = (int) r.stream().filter(m -> !m.isBye()).count();
            upperLosers.add(losers);
        }

        // Tour 0 (interne) : perdants T1 s'affrontent
        int matchCount = (int) Math.ceil(upperLosers.get(0) / 2.0);
        List<Match> t0 = new ArrayList<>();
        for (int i = 0; i < matchCount; i++) {
            t0.add(new Match(i,
                    "↓ " + upperName + " T1",
                    "↓ " + upperName + " T1"));
        }
        rounds.add(t0);

        int upperTourIdx = 1;
        while (matchCount > 1 || upperTourIdx < upperLosers.size()) {
            boolean isInternalTour = (rounds.size() % 2 == 0);
            if (isInternalTour) {
                // Tour interne : 2 → 1
                int prevCount = rounds.get(rounds.size() - 1).size();
                int newCount  = (int) Math.ceil(prevCount / 2.0);
                List<Match> round = new ArrayList<>();
                int tNum = rounds.size() + 1;
                for (int i = 0; i < newCount; i++) {
                    round.add(new Match(i,
                            "← Gagnant T" + tNum,
                            "← Gagnant T" + tNum));
                }
                rounds.add(round);
                matchCount = newCount;
            } else {
                // Tour avec descente : 1:1
                int prevCount = rounds.get(rounds.size() - 1).size();
                String dropLabel = upperTourIdx < upperLosers.size()
                        ? "↓ " + upperName + " T" + (upperTourIdx + 1)
                        : "↓ " + upperName + " T?";
                List<Match> round = new ArrayList<>();
                int tNum = rounds.size() + 1;
                for (int i = 0; i < prevCount; i++) {
                    round.add(new Match(i,
                            "← Gagnant T" + tNum,
                            dropLabel));
                }
                rounds.add(round);
                matchCount = prevCount;
                upperTourIdx++;
            }
            if (matchCount == 1 && upperTourIdx >= upperLosers.size()) break;
        }

        return rounds;
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private Tournament buildTournament(String name, int level,
                                       List<List<Match>> roundMatches, boolean hasDropRounds) {
        Tournament t = new Tournament(name, level);
        for (int r = 0; r < roundMatches.size(); r++) {
            boolean isDropRound = hasDropRounds && (r % 2 == 1);
            TournamentRound round = new TournamentRound(r, isDropRound);
            for (Match m : roundMatches.get(r)) {
                round.addMatch(m);
            }
            t.addRound(round);
        }
        return t;
    }

    private List<List<Match>> extractRounds(Tournament t) {
        List<List<Match>> result = new ArrayList<>();
        for (TournamentRound round : t.getRounds()) {
            result.add(new ArrayList<>(round.getMatches()));
        }
        return result;
    }
}