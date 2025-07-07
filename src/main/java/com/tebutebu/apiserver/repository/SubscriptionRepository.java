package com.tebutebu.apiserver.repository;

import com.tebutebu.apiserver.domain.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByMemberIdAndProjectIdAndDeletedAtIsNull(Long memberId, Long projectId);

    boolean existsByMemberIdAndProjectIdAndDeletedAtIsNull(Long memberId, Long projectId);

    Optional<Subscription> findByMemberIdAndProjectId(Long memberId, Long projectId);

    @Query("SELECT s.project.id from Subscription s " +
            "WHERE s.member.id = :memberId AND s.deletedAt IS NULL")
    List<Long> findSubscribedProjectIdsByMemberId(@Param("memberId") Long memberId);

}
