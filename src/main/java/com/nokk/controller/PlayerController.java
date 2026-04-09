package com.nokk.controller;

import com.nokk.dto.BonusDto;
import com.nokk.dto.PlayerDto;
import com.nokk.service.PlayerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    // GET /api/players
    @GetMapping
    public ResponseEntity<List<PlayerDto>> getAll() {
        return ResponseEntity.ok(playerService.getAllPlayers());
    }

    // GET /api/players/{id}
    @GetMapping("/{id}")
    public ResponseEntity<PlayerDto> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(playerService.getPlayer(id));
    }

    // POST /api/players
    @PostMapping
    public ResponseEntity<PlayerDto> create(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(playerService.createPlayer(name));
    }

    // PATCH /api/players/bonus
    @PatchMapping("/bonus")
    public ResponseEntity<PlayerDto> applyBonus(@Valid @RequestBody BonusDto dto) {
        return ResponseEntity.ok(playerService.applyBonus(dto));
    }
    // PATCH /api/players/{id}
    @PatchMapping("/{id}")
    public ResponseEntity<PlayerDto> updateName(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String name = body.get("name");
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(playerService.updateName(id, name));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        playerService.deletePlayer(id);
        return ResponseEntity.noContent().build();
    }
}
