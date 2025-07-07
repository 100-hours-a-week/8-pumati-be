package com.tebutebu.apiserver.repository;

import com.tebutebu.apiserver.domain.TeamBadgeStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamBadgeStatRepository extends JpaRepository<TeamBadgeStat, Long> {

    Optional<TeamBadgeStat> findByGiverTeamIdAndReceiverTeamId(Long giverTeamId, Long receiverTeamId);

    @Query("SELECT t.giverTeam.number, t.acquiredCount FROM TeamBadgeStat t " +
            "WHERE t.receiverTeam.id = :receiverTeamId ORDER BY t.acquiredCount DESC")
    List<Object[]> findReceivedBadgeStatsByReceiverTeamId(@Param("receiverTeamId") Long receiverTeamId);

}
