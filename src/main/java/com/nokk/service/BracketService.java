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

    public List<Tournament> initializeTournament(List<Player> players) {
        List<List<Match>> orRounds = buildOrRounds(players);
        int nbPlayers = players.size();

        Tournament or     = buildTournament("Or",     1, orRounds, false);
        Tournament argent = buildTournament("Argent", 2,
                buildLoserRounds(nbPlayers, "Or"), true);
        Tournament bronze = buildTournament("Bronze", 3,
                buildLoserRounds(nbPlayers / 2, "Argent"), true);

        tournamentRepository.save(or);
        tournamentRepository.save(argent);
        tournamentRepository.save(bronze);

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

    private List<List<Match>> buildOrRounds(List<Player> players) {
        List<List<Match>> rounds = new ArrayList<>();
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
        List<Match> prev = r1;
        while (prev.size() > 1) {
            int count = (int) Math.ceil(prev.size() / 2.0);
            List<Match> next = new ArrayList<>();
            for (int i = 0; i < count; i++) next.add(new Match(i));
            rounds.add(next);
            prev = next;
        }
        return rounds;
    }

    /**
     * Structure double-élimination loser bracket :
     *
     * R0 (interne) : nbPlayers/2 perdants s'affrontent → nbPlayers/4 matchs
     * R1 (drop)    : gagnants R0 + nbPlayers/4 nouveaux perdants WB
     * R2 (interne) : gagnants R1 s'affrontent
     * R3 (drop)    : gagnants R2 + nouveaux perdants WB
     * ...
     * Dernière étape : 1 match = finale loser
     *
     * Tours pairs  = internes  (2 gagnants LB → 1)
     * Tours impairs = drop     (1 gagnant LB + 1 perdant WB)
     */
    private List<List<Match>> buildLoserRounds(int nbPlayers, String upperName) {
        List<List<Match>> rounds = new ArrayList<>();

        // Nombre de matchs au premier tour interne
        int matchCount = Math.max(1, nbPlayers / 4);
        // Nombre de "vagues" de perdants WB encore à intégrer
        // WB R0 → LB R0, WB R1 → LB R1, WB R2 → LB R3, etc.
        int wbRound = 1; // prochain round WB dont les perdants arrivent

        // R0 : interne — perdants WB T1 s'affrontent
        List<Match> r0 = new ArrayList<>();
        for (int i = 0; i < matchCount; i++) {
            r0.add(new Match(i, "↓ " + upperName + " T1", "↓ " + upperName + " T1"));
        }
        rounds.add(r0);

        while (matchCount > 1) {
            int roundIdx = rounds.size();

            if (roundIdx % 2 == 1) {
                // Tour drop : chaque gagnant LB précédent reçoit un perdant WB
                String dropLabel = "↓ " + upperName + " T" + (wbRound + 1);
                List<Match> drop = new ArrayList<>();
                for (int i = 0; i < matchCount; i++) {
                    drop.add(new Match(i, "← Gagnant T" + roundIdx, dropLabel));
                }
                rounds.add(drop);
                wbRound++;
            } else {
                // Tour interne : les gagnants du drop s'affrontent 2 par 2
                int newCount = (int) Math.ceil(matchCount / 2.0);
                List<Match> internal = new ArrayList<>();
                int tNum = rounds.size();
                for (int i = 0; i < newCount; i++) {
                    internal.add(new Match(i,
                            "← Gagnant T" + tNum,
                            "← Gagnant T" + tNum));
                }
                rounds.add(internal);
                matchCount = newCount;
            }
        }

        return rounds;
    }

    private Tournament buildTournament(String name, int level,
                                       List<List<Match>> roundMatches, boolean hasDropRounds) {
        Tournament t = new Tournament(name, level);
        for (int r = 0; r < roundMatches.size(); r++) {
            boolean isDropRound = hasDropRounds && (r % 2 == 1);
            TournamentRound round = new TournamentRound(r, isDropRound);
            for (Match m : roundMatches.get(r)) round.addMatch(m);
            t.addRound(round);
        }
        return t;
    }
}