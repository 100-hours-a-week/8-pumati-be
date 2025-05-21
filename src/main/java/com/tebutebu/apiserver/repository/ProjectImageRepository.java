package com.tebutebu.apiserver.repository;

import com.tebutebu.apiserver.domain.ProjectImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectImageRepository extends JpaRepository<ProjectImage, Long> {

    @Query("SELECT pi FROM ProjectImage pi WHERE pi.project.id = :projectId ORDER BY pi.sequence ASC")
    Optional<List<ProjectImage>> findByProjectIdOrderBySequenceAsc(@Param("projectId") Long projectId);

    @Query("SELECT pi FROM ProjectImage pi WHERE pi.project.id = :projectId AND pi.sequence = :sequence")
    Optional<ProjectImage> findByProjectIdAndSequence(@Param("projectId") Long projectId, @Param("sequence") Integer sequence);

    void deleteAllByProjectId(Long projectId);

}
