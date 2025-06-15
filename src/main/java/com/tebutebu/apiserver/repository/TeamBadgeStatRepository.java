package com.tebutebu.apiserver.repository;

import com.tebutebu.apiserver.domain.TeamBadgeStat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamBadgeStatRepository extends JpaRepository<TeamBadgeStat, Long> {

    Optional<TeamBadgeStat> findByGiverTeamIdAndReceiverTeamId(Long giverTeamId, Long receiverTeamId);

}
