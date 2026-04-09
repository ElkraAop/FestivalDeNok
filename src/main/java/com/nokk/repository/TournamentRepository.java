package com.nokk.repository;

import com.nokk.model.Tournament;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {

    Optional<Tournament> findByLevel(int level);

    @EntityGraph(attributePaths = {"rounds", "rounds.matches", "rounds.matches.teamA", "rounds.matches.teamB"})
    @Query("SELECT t FROM Tournament t WHERE t.id = :id")
    Optional<Tournament> findByIdFull(Long id);

    @EntityGraph(attributePaths = {"rounds", "rounds.matches", "rounds.matches.teamA", "rounds.matches.teamB"})
    @Query("SELECT t FROM Tournament t ORDER BY t.level ASC")
    List<Tournament> findAllFull();

    @EntityGraph(attributePaths = {"rounds", "rounds.matches", "rounds.matches.teamA", "rounds.matches.teamB"})
    @Query("SELECT t FROM Tournament t WHERE t.level = :level")
    Optional<Tournament> findByLevelFull(int level);
}