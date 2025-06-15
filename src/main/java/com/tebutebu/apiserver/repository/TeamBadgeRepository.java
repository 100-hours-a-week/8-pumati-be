package com.tebutebu.apiserver.repository;

import com.tebutebu.apiserver.domain.TeamBadge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamBadgeRepository extends JpaRepository<TeamBadge, Long> {

    Optional<TeamBadge> findByGiverTeamIdAndReceiverTeamId(Long giverTeamId, Long receiverTeamId);

}
