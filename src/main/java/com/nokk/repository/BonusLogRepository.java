package com.nokk.repository;

import com.nokk.model.BonusLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BonusLogRepository extends JpaRepository<BonusLog, Long> {

    List<BonusLog> findByPlayerIdOrderByCreatedAtDesc(Long playerId);

    void deleteByPlayerId(Long playerId);
}
