package com.tebutebu.apiserver.service.subscription;

import com.tebutebu.apiserver.domain.Member;
import com.tebutebu.apiserver.domain.Project;
import com.tebutebu.apiserver.domain.Subscription;
import com.tebutebu.apiserver.global.errorcode.BusinessErrorCode;
import com.tebutebu.apiserver.global.exception.BusinessException;
import com.tebutebu.apiserver.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    @Override
    public Long subscribe(Long memberId, Long projectId) {
        boolean exists = subscriptionRepository.existsByMemberIdAndProjectIdAndDeletedAtIsNull(memberId, projectId);
        if (exists) {
            throw new BusinessException(BusinessErrorCode.ALREADY_SUBSCRIBED);
        }

        Optional<Subscription> deletedSubscription = subscriptionRepository
                .findByMemberIdAndProjectId(memberId, projectId);

        if (deletedSubscription.isPresent()) {
            Subscription sub = deletedSubscription.get();
            sub.restore();
            return sub.getId();
        }

        Member member = Member.builder().id(memberId).build();
        Project project = Project.builder().id(projectId).build();

        Subscription subscription = Subscription.builder()
                .member(member)
                .project(project)
                .subscribedAt(LocalDateTime.now())
                .build();

        return subscriptionRepository.save(subscription).getId();
    }

    @Override
    public void unsubscribe(Long memberId, Long projectId) {
        Subscription subscription = subscriptionRepository.findByMemberIdAndProjectIdAndDeletedAtIsNull(memberId, projectId)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.SUBSCRIPTION_NOT_FOUND));

        subscription.unsubscribe(LocalDateTime.now());
    }

}
