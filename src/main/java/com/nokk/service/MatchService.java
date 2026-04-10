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

        match.setScoreA(dto.getScoreA());
        match.setScoreB(dto.getScoreB());
        match.setFinished(dto.isFinished());

        if (dto.isFinished() && match.getLoser() != null && !match.isLoserDropped()) {
            dropLoser(dto.getTournamentId(), dto.getRoundIndex(), dto.getMatchIndex(), match.getLoser());
            match.setLoserDropped(true);
        }

        propagateWinner(tournament, dto.getRoundIndex(), dto.getMatchIndex());
        tournamentRepository.save(tournament);

        return toDto(tournamentRepository.findByIdFull(dto.getTournamentId()).orElseThrow());
    }

    private int wbToLbRoundIndex(int wbRoundIndex) {
        return wbRoundIndex == 0 ? 0 : 2 * wbRoundIndex - 1;
    }

    private void dropLoser(Long fromTournamentId, int wbRoundIndex, int wbMatchIndex, Player loser) {
        Tournament from = tournamentRepository.findByIdFull(fromTournamentId).orElseThrow();
        Tournament target = tournamentRepository.findByLevelFull(from.getLevel() + 1).orElse(null);
        if (target == null) return;

        int lbRoundIndex = wbToLbRoundIndex(wbRoundIndex);

        TournamentRound targetRound = target.getRounds().stream()
                .filter(r -> r.getRoundIndex() == lbRoundIndex)
                .findFirst().orElse(null);
        if (targetRound == null) return;

        List<Match> matches = targetRound.getMatches().stream().toList();

        if (lbRoundIndex == 0) {
            for (Match m : matches) {
                if (m.isFinished()) continue;
                if (m.getTeamA() == null) { m.setTeamA(loser); break; }
                if (m.getTeamB() == null) { m.setTeamB(loser); break; }
            }
        } else {
            Match direct = matches.stream()
                    .filter(m -> m.getMatchIndex() == wbMatchIndex)
                    .findFirst().orElse(null);
            if (direct != null && !direct.isFinished() && direct.getTeamB() == null) {
                direct.setTeamB(loser);
            } else {
                for (Match fallback : matches) {
                    if (!fallback.isFinished() && fallback.getTeamB() == null) {
                        fallback.setTeamB(loser);
                        break;
                    }
                }
            }
        }

        tournamentRepository.save(target);
    }

    private void removeFromLowerBracket(Long fromTournamentId, int wbRoundIndex, Player player) {
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
        List<TournamentRound> rounds = t.getRounds().stream().toList();
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
            int nextMatchIdx = fromMatchIdx / 2;
            Match next = nextRound.getMatches().stream()
                    .filter(m -> m.getMatchIndex() == nextMatchIdx)
                    .findFirst().orElse(null);
            if (next == null || next.isFinished()) return;
            if (fromMatchIdx % 2 == 0) next.setTeamA(winner);
            else                        next.setTeamB(winner);
        } else {
            if (nextRound.isDropRound()) {
                Match next = nextRound.getMatches().stream()
                        .filter(m -> m.getMatchIndex() == fromMatchIdx)
                        .findFirst().orElse(null);
                if (next != null && !next.isFinished()) next.setTeamA(winner);
            } else {
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
                                            .sorted((a, b) -> Integer.compare(a.getMatchIndex(), b.getMatchIndex()))
                                            .map(m -> {
                                                TournamentDto.MatchNodeDto mn = new TournamentDto.MatchNodeDto();
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
                                                    mn.setTeamAId(m.getTeamA().getId());
                                                    mn.setTeamAName(m.getTeamA().getName());
                                                }
                                                if (m.getTeamB() != null) {
                                                    mn.setTeamBId(m.getTeamB().getId());
                                                    mn.setTeamBName(m.getTeamB().getName());
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