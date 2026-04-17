package com.nokk.service;

import com.nokk.dto.MatchDto;
import com.nokk.dto.TournamentDto;
import com.nokk.model.*;
import com.nokk.repository.MatchRepository;
import com.nokk.repository.PlayerRepository;
import com.nokk.repository.TournamentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MatchService {

    private final MatchRepository      matchRepository;
    private final TournamentRepository tournamentRepository;
    private final PlayerRepository     playerRepository;

    public MatchService(MatchRepository matchRepository,
                        TournamentRepository tournamentRepository,
                        PlayerRepository playerRepository) {
        this.matchRepository      = matchRepository;
        this.tournamentRepository = tournamentRepository;
        this.playerRepository     = playerRepository;
    }

    public TournamentDto updateMatch(MatchDto dto) {
        Tournament tournament = tournamentRepository.findByIdFull(dto.getTournamentId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Tournament introuvable : " + dto.getTournamentId()));

        TournamentRound round = tournament.getRounds().stream()
                .filter(r -> r.getRoundIndex() == dto.getRoundIndex())
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "Tour introuvable : " + dto.getRoundIndex()));

        Match match = round.getMatches().stream()
                .filter(m -> m.getMatchIndex() == dto.getMatchIndex())
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "Match introuvable : " + dto.getMatchIndex()));

        if (match.isFinished() && match.isLoserDropped() && match.getLoser() != null) {
            removeFromLowerBracket(dto.getTournamentId(), dto.getRoundIndex(), match.getLoser());
            match.setLoserDropped(false);
        }

        if (dto.getNewTeamAId() != null) {
            Player p = playerRepository.findById(dto.getNewTeamAId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Joueur introuvable : " + dto.getNewTeamAId()));
            match.setTeamA(p);
        }
        if (dto.getNewTeamBId() != null) {
            Player p = playerRepository.findById(dto.getNewTeamBId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Joueur introuvable : " + dto.getNewTeamBId()));
            match.setTeamB(p);
        }

        // Match solo (teamB absent) : forcer teamA gagnante
        if (dto.isFinished() && match.getTeamA() != null && match.getTeamB() == null) {
            match.setScoreA(1);
            match.setScoreB(0);
            match.setBye(true);
        } else {
            match.setScoreA(dto.getScoreA());
            match.setScoreB(dto.getScoreB());
        }
        match.setFinished(dto.isFinished());

        if (dto.isFinished() && match.getLoser() != null && !match.isLoserDropped()) {
            dropLoser(dto.getTournamentId(), dto.getRoundIndex(), dto.getMatchIndex(), match.getLoser());
            match.setLoserDropped(true);
        }

        propagateWinner(tournament, dto.getRoundIndex(), dto.getMatchIndex());
        tournamentRepository.save(tournament);

        return toDto(tournamentRepository.findByIdFull(dto.getTournamentId()).orElseThrow());
    }

    // WB R0 → LB R0, WB R1 → LB R1, WB R2 → LB R3, WB Rn → LB R(2n-1)
    private int wbToLbRoundIndex(int wbRoundIndex) {
        return wbRoundIndex == 0 ? 0 : 2 * wbRoundIndex - 1;
    }

    private void dropLoser(Long fromTournamentId, int wbRoundIndex,
                           int wbMatchIndex, Player loser) {
        Tournament from = tournamentRepository.findByIdFull(fromTournamentId).orElseThrow();
        Tournament target = tournamentRepository.findByLevelFull(from.getLevel() + 1).orElse(null);
        if (target == null) return;

        int lbRoundIndex = wbToLbRoundIndex(wbRoundIndex);

        TournamentRound targetRound = target.getRounds().stream()
                .filter(r -> r.getRoundIndex() == lbRoundIndex)
                .findFirst().orElse(null);
        if (targetRound == null) return;

        List<Match> matches = targetRound.getMatches().stream()
                .sorted((a, b) -> Integer.compare(a.getMatchIndex(), b.getMatchIndex()))
                .toList();

        if (lbRoundIndex == 0) {
            // R0 interne : on remplit teamA puis teamB dans l'ordre
            for (Match m : matches) {
                if (m.isFinished() || m.isBye()) continue;
                if (m.getTeamA() == null) { m.setTeamA(loser); break; }
                if (m.getTeamB() == null) { m.setTeamB(loser); break; }
            }
        } else {
            // Tours drop : le perdant WB[wbMatchIndex] → LB[wbMatchIndex] en teamB
            // Si le match direct est déjà pris, on cherche le premier libre
            Match direct = matches.stream()
                    .filter(m -> m.getMatchIndex() == wbMatchIndex
                            && !m.isFinished() && !m.isBye()
                            && m.getTeamB() == null)
                    .findFirst().orElse(null);

            if (direct != null) {
                direct.setTeamB(loser);
            } else {
                for (Match fallback : matches) {
                    if (!fallback.isFinished() && !fallback.isBye()
                            && fallback.getTeamB() == null) {
                        fallback.setTeamB(loser);
                        break;
                    }
                }
            }
        }

        tournamentRepository.save(target);
    }

    private void removeFromLowerBracket(Long fromTournamentId,
                                        int wbRoundIndex, Player player) {
        Tournament from = tournamentRepository.findByIdFull(fromTournamentId).orElseThrow();
        Tournament target = tournamentRepository.findByLevelFull(from.getLevel() + 1).orElse(null);
        if (target == null) return;

        int lbRoundIndex = wbToLbRoundIndex(wbRoundIndex);

        TournamentRound targetRound = target.getRounds().stream()
                .filter(r -> r.getRoundIndex() == lbRoundIndex)
                .findFirst().orElse(null);
        if (targetRound == null) return;

        for (Match m : targetRound.getMatches()) {
            if (m.isFinished()) continue;
            if (player.equals(m.getTeamA())) {
                m.setTeamA(m.getTeamB());
                m.setTeamB(null);
                tournamentRepository.save(target);
                return;
            }
            if (player.equals(m.getTeamB())) {
                m.setTeamB(null);
                tournamentRepository.save(target);
                return;
            }
        }
    }

    void propagateWinner(Tournament t, int fromRoundIdx, int fromMatchIdx) {
        List<TournamentRound> rounds = t.getRounds().stream()
                .sorted((a, b) -> Integer.compare(a.getRoundIndex(), b.getRoundIndex()))
                .toList();
        if (fromRoundIdx >= rounds.size() - 1) return;

        TournamentRound currentRound = rounds.stream()
                .filter(r -> r.getRoundIndex() == fromRoundIdx)
                .findFirst().orElse(null);
        if (currentRound == null) return;

        TournamentRound nextRound = rounds.stream()
                .filter(r -> r.getRoundIndex() == fromRoundIdx + 1)
                .findFirst().orElse(null);
        if (nextRound == null) return;

        Match current = currentRound.getMatches().stream()
                .filter(m -> m.getMatchIndex() == fromMatchIdx)
                .findFirst().orElse(null);
        if (current == null || current.getWinner() == null) return;

        Player winner = current.getWinner();

        if (t.getLevel() == 1) {
            // WB : match i et i+1 → match i/2 du tour suivant
            int nextMatchIdx = fromMatchIdx / 2;
            Match next = nextRound.getMatches().stream()
                    .filter(m -> m.getMatchIndex() == nextMatchIdx)
                    .findFirst().orElse(null);
            if (next == null || next.isFinished()) return;
            if (fromMatchIdx % 2 == 0) next.setTeamA(winner);
            else                        next.setTeamB(winner);

            // Cascade bye : si le match frère (index pair+1) n'existe pas dans le tour courant,
            // teamB ne sera jamais fourni → bye structurel sur le match destination
            if (fromMatchIdx % 2 == 0) {
                boolean siblingExists = currentRound.getMatches().stream()
                        .anyMatch(m -> m.getMatchIndex() == fromMatchIdx + 1);
                if (!siblingExists && !next.isFinished()) {
                    next.setScoreA(1);
                    next.setScoreB(0);
                    next.setFinished(true);
                    next.setBye(true);
                    propagateWinner(t, fromRoundIdx + 1, nextMatchIdx);
                }
            }

        } else {
            if (nextRound.isDropRound()) {
                // LB tour interne → tour drop : même index
                Match next = nextRound.getMatches().stream()
                        .filter(m -> m.getMatchIndex() == fromMatchIdx
                                && !m.isFinished())
                        .findFirst().orElse(null);
                if (next != null) next.setTeamA(winner);
            } else {
                // LB tour drop → tour interne : match i et i+1 → match i/2
                int nextMatchIdx = fromMatchIdx / 2;
                Match next = nextRound.getMatches().stream()
                        .filter(m -> m.getMatchIndex() == nextMatchIdx)
                        .findFirst().orElse(null);
                if (next == null || next.isFinished()) return;
                if (fromMatchIdx % 2 == 0) next.setTeamA(winner);
                else                        next.setTeamB(winner);
            }
        }
    }

    public TournamentDto toDto(Tournament t) {
        TournamentDto dto = new TournamentDto();
        dto.setId(t.getId());
        dto.setName(t.getName());
        dto.setLevel(t.getLevel());
        dto.setRounds(
                t.getRounds().stream()
                        .sorted((a, b) -> Integer.compare(a.getRoundIndex(), b.getRoundIndex()))
                        .map(round -> {
                            TournamentDto.RoundDto r = new TournamentDto.RoundDto();
                            r.setId(round.getId());
                            r.setRoundIndex(round.getRoundIndex());
                            r.setDropRound(round.isDropRound());
                            r.setMatches(
                                    round.getMatches().stream()
                                            .sorted((a, b) -> Integer.compare(
                                                    a.getMatchIndex(), b.getMatchIndex()))
                                            .map(m -> {
                                                TournamentDto.MatchNodeDto mn =
                                                        new TournamentDto.MatchNodeDto();
                                                mn.setId(m.getId());
                                                mn.setMatchIndex(m.getMatchIndex());
                                                mn.setScoreA(m.getScoreA());
                                                mn.setScoreB(m.getScoreB());
                                                mn.setFinished(m.isFinished());
                                                mn.setBye(m.isBye());
                                                mn.setLoserDropped(m.isLoserDropped());
                                                mn.setTeamAOrigin(m.getTeamAOrigin());
                                                mn.setTeamBOrigin(m.getTeamBOrigin());
                                                if (m.getTeamA() != null) {
                                                    Player a = m.getTeamA();
                                                    mn.setTeamAId(a.getId());
                                                    mn.setTeamAName(a.getName());
                                                    mn.setTeamALives(a.getLives());
                                                    mn.setTeamADoubleHits(a.getDoubleHits());
                                                    mn.setTeamAWeapon(a.getWeapon().name());
                                                    if (a.hasCard()) {
                                                        mn.setTeamACard(a.getCardSuit().name() + " " + a.getCardValue());
                                                    }
                                                }
                                                if (m.getTeamB() != null) {
                                                    Player b = m.getTeamB();
                                                    mn.setTeamBId(b.getId());
                                                    mn.setTeamBName(b.getName());
                                                    mn.setTeamBLives(b.getLives());
                                                    mn.setTeamBDoubleHits(b.getDoubleHits());
                                                    mn.setTeamBWeapon(b.getWeapon().name());
                                                    if (b.hasCard()) {
                                                        mn.setTeamBCard(b.getCardSuit().name() + " " + b.getCardValue());
                                                    }
                                                }
                                                return mn;
                                            }).toList()
                            );
                            return r;
                        }).toList()
        );
        return dto;
    }
}