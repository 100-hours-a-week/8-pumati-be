package com.tebutebu.apiserver.repository;

import com.tebutebu.apiserver.domain.ProjectRankingSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

public interface ProjectRankingSnapshotRepository extends JpaRepository<ProjectRankingSnapshot, Long> {

    Optional<ProjectRankingSnapshot> findTopByRequestedAtAfterOrderByRequestedAtDesc(LocalDateTime time);

    Optional<ProjectRankingSnapshot> findTopByOrderByRequestedAtDesc();

    List<ProjectRankingSnapshot> findAllByRequestedAtBetween(LocalDateTime start, LocalDateTime end);

}
