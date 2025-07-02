package com.tebutebu.apiserver.service.subscription;

import com.tebutebu.apiserver.domain.Member;
import com.tebutebu.apiserver.domain.Project;
import com.tebutebu.apiserver.domain.Subscription;
import com.tebutebu.apiserver.dto.member.response.MemberResponseDTO;
import com.tebutebu.apiserver.global.exception.BusinessException;
import com.tebutebu.apiserver.global.errorcode.BusinessErrorCode;
import com.tebutebu.apiserver.repository.SubscriptionRepository;
import com.tebutebu.apiserver.service.member.MemberService;
import com.tebutebu.apiserver.service.project.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    private final MemberService memberService;

    @Override
    @Transactional
    public Long subscribe(Long memberId, Long projectId) {
        boolean exists = subscriptionRepository.existsByMemberIdAndProjectIdAndDeletedAtIsNull(memberId, projectId);
        if (exists) {
            throw new BusinessException(BusinessErrorCode.ALREADY_SUBSCRIBED);
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
    @Transactional
    public void unsubscribe(Long memberId, Long projectId) {
        Subscription subscription = subscriptionRepository.findByMemberIdAndProjectIdAndDeletedAtIsNull(memberId, projectId)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.SUBSCRIPTION_NOT_FOUND));

        subscription.unsubscribe(LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSubscribed(Long memberId, Long projectId) {
        return subscriptionRepository.existsByMemberIdAndProjectIdAndDeletedAtIsNull(memberId, projectId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberResponseDTO> getSubscribers(Long projectId) {
        List<Subscription> subscriptions = subscriptionRepository.findAllByProjectIdAndDeletedAtIsNull(projectId);
        return subscriptions.stream()
                .map(Subscription::getMember)
                .map(memberService::entityToDTO)
                .toList();
    }

}
