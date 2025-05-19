package com.tebutebu.apiserver.repository;

import com.tebutebu.apiserver.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c " +
            "JOIN FETCH c.project " +
            "JOIN FETCH c.member " +
            "WHERE c.id = :id")
    Optional<Comment> findByIdWithMemberAndProject(@Param("id") Long id);

    long countByProjectId(Long projectId);

}
