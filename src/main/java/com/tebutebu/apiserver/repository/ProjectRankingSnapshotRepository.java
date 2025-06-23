package com.tebutebu.apiserver.repository;

import com.tebutebu.apiserver.domain.ProjectRankingSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectRankingSnapshotRepository extends JpaRepository<ProjectRankingSnapshot, Long> {

    Optional<ProjectRankingSnapshot> findTopByOrderByRequestedAtDesc();

}
