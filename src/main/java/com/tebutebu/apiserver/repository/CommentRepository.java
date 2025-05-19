package com.tebutebu.apiserver.repository;

import com.tebutebu.apiserver.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c " +
            "JOIN FETCH c.project " +
            "JOIN FETCH c.member " +
            "WHERE c.id = :id")
    Optional<Comment> findByIdWithMemberAndProject(@Param("id") Long id);

    interface ProjectCommentCount {
        Long getProjectId();
        Long getCount();
    }

    @Query("SELECT c.project.id AS projectId, " +
            "COUNT(c) AS count " +
            "FROM Comment c " +
            "WHERE c.project.id IN :projectIds " +
            "GROUP BY c.project.id")
    List<ProjectCommentCount> countByProjectIds(@Param("projectIds") List<Long> projectIds);

    default Map<Long, Long> findCommentCountMap(List<Long> projectIds) {
        return countByProjectIds(projectIds).stream()
                .collect(Collectors.toMap(
                        ProjectCommentCount::getProjectId,
                        ProjectCommentCount::getCount
                ));
    }

}
