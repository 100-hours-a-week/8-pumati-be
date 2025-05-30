package com.tebutebu.apiserver.repository;

import com.tebutebu.apiserver.domain.MemberTeamBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MemberTeamBadgeRepository extends JpaRepository<MemberTeamBadge, Long> {

    @Query("SELECT b " +
            "FROM MemberTeamBadge b " +
            "JOIN FETCH b.member m " +
            "JOIN FETCH b.team t " +
            "WHERE m.id = :memberId " +
            "AND t.id = :teamId")
    Optional<MemberTeamBadge> findByMemberIdAndTeamId(Long memberId, Long teamId);

}
