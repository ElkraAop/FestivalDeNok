package com.nokk.controller;

import com.nokk.dto.MatchDto;
import com.nokk.dto.TournamentDto;
import com.nokk.service.MatchService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    /**
     * PUT /api/matches
     * Met à jour un match et retourne le bracket complet mis à jour.
     * Le bracket retourné inclut les propagations (gagnant au tour suivant,
     * perdant dans le bracket inférieur).
     */
    @PutMapping
    public ResponseEntity<TournamentDto> updateMatch(@Valid @RequestBody MatchDto dto) {
        return ResponseEntity.ok(matchService.updateMatch(dto));
    }
}
