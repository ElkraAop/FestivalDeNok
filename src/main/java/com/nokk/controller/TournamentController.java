package com.nokk.controller;

import com.nokk.dto.TournamentDto;
import com.nokk.service.TournamentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tournaments")
public class TournamentController {

    private final TournamentService tournamentService;

    public TournamentController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    // GET /api/tournaments
    @GetMapping
    public ResponseEntity<List<TournamentDto>> getAll() {
        return ResponseEntity.ok(tournamentService.getAllTournaments());
    }

    // GET /api/tournaments/{id}
    @GetMapping("/{id}")
    public ResponseEntity<TournamentDto> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(tournamentService.getTournament(id));
    }

    // POST /api/tournaments/init
    // Lance le tournoi avec tous les joueurs présents en BDD
    @PostMapping("/init")
    public ResponseEntity<List<TournamentDto>> initialize() {
        return ResponseEntity.ok(tournamentService.initializeTournament());
    }

    // DELETE /api/tournaments/reset
    @DeleteMapping("/reset")
    public ResponseEntity<Void> reset() {
        tournamentService.resetTournament();
        return ResponseEntity.noContent().build();
    }
}
