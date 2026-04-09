package com.nokk.repository;

import com.nokk.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    Optional<Player> findByName(String name);

    boolean existsByName(String name);

    // Charge le joueur avec son historique en une seule requête
    @Query("SELECT p FROM Player p LEFT JOIN FETCH p.bonusHistory WHERE p.id = :id")
    Optional<Player> findByIdWithHistory(Long id);

    // Tous les joueurs avec leur historique (évite N+1)
    @Query("SELECT DISTINCT p FROM Player p LEFT JOIN FETCH p.bonusHistory")
    List<Player> findAllWithHistory();
}
