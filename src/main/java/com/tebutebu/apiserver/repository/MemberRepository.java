package com.tebutebu.apiserver.repository;

import com.tebutebu.apiserver.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Member findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT m FROM Member m LEFT JOIN FETCH m.team WHERE m.id = :id")
    Optional<Member> findByIdWithTeam(@Param("id") Long id);

    @Query("SELECT m FROM Member m JOIN FETCH m.team WHERE m.team.id = :teamId")
    List<Member> findAllByTeamIdWithTeam(@Param("teamId") Long teamId);

}
