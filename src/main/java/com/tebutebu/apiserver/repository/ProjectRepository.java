package com.tebutebu.apiserver.repository;

import com.tebutebu.apiserver.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("SELECT DISTINCT p FROM Project p "
            + "LEFT JOIN FETCH p.team t "
            + "LEFT JOIN FETCH p.images i "
            + "WHERE p.id = :id")
    Optional<Project> findProjectWithTeamAndImagesById(@Param("id") Long id);

    @Query("SELECT p.id FROM Project p WHERE p.team.id = :teamId")
    Optional<Long> findProjectIdByTeamId(@Param("teamId") Long teamId);

    @Query("SELECT DISTINCT p "
            + "FROM Project p "
            + "LEFT JOIN FETCH p.team t "
            + "LEFT JOIN FETCH p.images i "
            + "WHERE t.id = :teamId")
    Optional<Project> findProjectByTeamId(Long teamId);

    boolean existsByTeamId(Long teamId);

    @Query("SELECT DISTINCT p FROM Project p " +
            "LEFT JOIN FETCH p.team t " +
            "LEFT JOIN FETCH p.images i " +
            "ORDER BY t.givedPumatiCount DESC")
    List<Project> findAllForRanking();

    @Query("SELECT MAX(p.createdAt) FROM Project p")
    Optional<LocalDateTime> findLatestCreatedAt();

}
