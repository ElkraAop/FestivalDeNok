package com.nokk.service;

import com.nokk.dto.TournamentDto;
import com.nokk.model.Player;
import com.nokk.model.Tournament;
import com.nokk.repository.PlayerRepository;
import com.nokk.repository.TournamentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TournamentService {

    private final TournamentRepository tournamentRepository;
    private final PlayerRepository     playerRepository;
    private final BracketService       bracketService;
    private final MatchService         matchService;

    public TournamentService(TournamentRepository tournamentRepository,
                             PlayerRepository playerRepository,
                             BracketService bracketService,
                             MatchService matchService) {
        this.tournamentRepository = tournamentRepository;
        this.playerRepository     = playerRepository;
        this.bracketService       = bracketService;
        this.matchService         = matchService;
    }

    @Transactional(readOnly = true)
    public List<TournamentDto> getAllTournaments() {
        return tournamentRepository.findAllFull()
                .stream()
                .sorted((a, b) -> Integer.compare(a.getLevel(), b.getLevel()))
                .map(matchService::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public TournamentDto getTournament(Long id) {
        return matchService.toDto(
                tournamentRepository.findByIdFull(id)
                        .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                                "Tournament introuvable : " + id))
        );
    }

    public List<TournamentDto> initializeTournament() {
        if (tournamentRepository.count() > 0) {
            throw new IllegalStateException("Le tournoi est déjà initialisé.");
        }
        List<Player> players = playerRepository.findAll();
        if (players.isEmpty()) {
            throw new IllegalStateException("Aucun joueur enregistré.");
        }
        return bracketService.initializeTournament(players)
                .stream()
                .map(matchService::toDto)
                .toList();
    }

    public void resetTournament() {
        tournamentRepository.deleteAll();
    }
}