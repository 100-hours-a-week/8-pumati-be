package com.tebutebu.apiserver.repository;

import com.tebutebu.apiserver.domain.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByMemberIdAndProjectIdAndDeletedAtIsNull(Long memberId, Long projectId);

    List<Subscription> findAllByProjectIdAndDeletedAtIsNull(Long projectId);

    boolean existsByMemberIdAndProjectIdAndDeletedAtIsNull(Long memberId, Long projectId);

}
