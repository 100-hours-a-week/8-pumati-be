package com.tebutebu.apiserver.repository;

import com.tebutebu.apiserver.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("SELECT DISTINCT p FROM Project p "
            + "LEFT JOIN FETCH p.team t "
            + "LEFT JOIN FETCH p.images i "
            + "WHERE p.id = :id")
    Optional<Project> findProjectWithTeamAndImagesById(@Param("id") Long id);

    boolean existsByTeamId(Long teamId);

    @Query("SELECT DISTINCT p FROM Project p " +
            "LEFT JOIN FETCH p.team t " +
            "LEFT JOIN FETCH p.images i " +
            "ORDER BY t.givedPumatiCount DESC")
    List<Project> findAllForRanking();

}
