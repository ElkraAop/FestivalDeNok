package com.nokk.repository;

import com.nokk.model.Match;
import com.nokk.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    @Query("""
        SELECT m FROM Match m
        LEFT JOIN FETCH m.teamA
        LEFT JOIN FETCH m.teamB
        WHERE m.round.id = :roundId
        ORDER BY m.matchIndex ASC
        """)
    List<Match> findByRoundId(Long roundId);

    @Query("""
        SELECT m FROM Match m
        LEFT JOIN FETCH m.teamA
        LEFT JOIN FETCH m.teamB
        WHERE m.teamA = :player OR m.teamB = :player
        """)
    List<Match> findByPlayer(Player player);
}