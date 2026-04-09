package com.nokk.service;

import com.nokk.dto.BonusDto;
import com.nokk.dto.PlayerDto;
import com.nokk.model.BonusLog;
import com.nokk.model.Player;
import com.nokk.repository.PlayerRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PlayerService {

    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    // ── Lecture ───────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<PlayerDto> getAllPlayers() {
        return playerRepository.findAllWithHistory()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PlayerDto getPlayer(Long id) {
        Player player = playerRepository.findByIdWithHistory(id)
                .orElseThrow(() -> new EntityNotFoundException("Joueur introuvable : " + id));
        return toDto(player);
    }

    // ── Création ──────────────────────────────────────────────────────────
    public PlayerDto createPlayer(String name) {
        if (playerRepository.existsByName(name)) {
            throw new IllegalArgumentException("Un joueur avec ce nom existe déjà : " + name);
        }
        Player player = new Player(name);
        return toDto(playerRepository.save(player));
    }
    public PlayerDto updateName(Long id, String newName) {
        if (playerRepository.existsByName(newName)) {
            throw new IllegalArgumentException(
                    "Un joueur avec le nom \"" + newName + "\" existe déjà.");
        }
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Joueur introuvable : " + id));
        player.setName(newName);
        return toDto(playerRepository.save(player));
    }
    public void deletePlayer(Long id) {
        if (!playerRepository.existsById(id)) {
            throw new EntityNotFoundException("Joueur introuvable : " + id);
        }
        playerRepository.deleteById(id);
    }

    // ── Bonus ─────────────────────────────────────────────────────────────
    public PlayerDto applyBonus(BonusDto dto) {
        Player player = playerRepository.findByIdWithHistory(dto.getPlayerId())
                .orElseThrow(() -> new EntityNotFoundException("Joueur introuvable : " + dto.getPlayerId()));

        if (dto.isReset()) {
            player.reset(dto.getNote());
            return toDto(playerRepository.save(player));
        }

        if (dto.getLivesDelta() != null) {
            int oldLives = player.getLives();
            player.setLives(player.getLives() + dto.getLivesDelta());
            player.getBonusHistory().add(new BonusLog(player,
                    "❤️ PV " + (dto.getLivesDelta() >= 0 ? "+" : "") + dto.getLivesDelta()
                    + " → " + player.getLives() + " PV"
                    + note(dto.getNote())));
        }

        if (dto.getDoubleHitsDelta() != null) {
            player.setDoubleHits(player.getDoubleHits() + dto.getDoubleHitsDelta());
            player.getBonusHistory().add(new BonusLog(player,
                    "💥 Coups doubles " + (dto.getDoubleHitsDelta() >= 0 ? "+" : "")
                    + dto.getDoubleHitsDelta() + " → " + player.getDoubleHits()
                    + note(dto.getNote())));
        }

        if (dto.getWeapon() != null) {
            String old = player.getWeapon().name();
            player.setWeapon(dto.getWeapon());
            player.getBonusHistory().add(new BonusLog(player,
                    "⚔️ Arme : " + old + " → " + dto.getWeapon().name()
                    + note(dto.getNote())));
        }

        if (dto.getCardSuit() != null && dto.getCardValue() != null) {
            player.setCardSuit(dto.getCardSuit());
            player.setCardValue(dto.getCardValue());
            player.getBonusHistory().add(new BonusLog(player, "🃏 Carte piochée"));
        }

        return toDto(playerRepository.save(player));
    }

    // ── Mapping ───────────────────────────────────────────────────────────
    public PlayerDto toDto(Player p) {
        PlayerDto dto = new PlayerDto();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setLives(p.getLives());
        dto.setDoubleHits(p.getDoubleHits());
        dto.setWeapon(p.getWeapon());
        dto.setCardSuit(p.getCardSuit());
        dto.setCardValue(p.getCardValue());
        dto.setHasCard(p.hasCard());
        dto.setBonusHistory(
            p.getBonusHistory().stream().map(log -> {
                PlayerDto.BonusLogDto l = new PlayerDto.BonusLogDto();
                l.setId(log.getId());
                l.setDescription(log.getDescription());
                l.setCreatedAt(log.getCreatedAt());
                return l;
            }).collect(Collectors.toList())
        );
        return dto;
    }

    private String note(String note) {
        return (note != null && !note.isBlank()) ? " (" + note + ")" : "";
    }
}
