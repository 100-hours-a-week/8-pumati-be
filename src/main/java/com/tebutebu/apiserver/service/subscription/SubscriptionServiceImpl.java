package com.tebutebu.apiserver.service.subscription;

import com.tebutebu.apiserver.domain.Member;
import com.tebutebu.apiserver.domain.Project;
import com.tebutebu.apiserver.domain.Subscription;
import com.tebutebu.apiserver.dto.mail.request.MailSendRequestDTO;
import com.tebutebu.apiserver.dto.member.response.MemberResponseDTO;
import com.tebutebu.apiserver.global.errorcode.BusinessErrorCode;
import com.tebutebu.apiserver.global.exception.BusinessException;
import com.tebutebu.apiserver.repository.SubscriptionRepository;
import com.tebutebu.apiserver.service.mail.kafka.producer.MailSendProducer;
import com.tebutebu.apiserver.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    private final MemberService memberService;

    private final MailSendProducer mailSendProducer;

    @Override
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
    public void unsubscribe(Long memberId, Long projectId) {
        Subscription subscription = subscriptionRepository.findByMemberIdAndProjectIdAndDeletedAtIsNull(memberId, projectId)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.SUBSCRIPTION_NOT_FOUND));

        subscription.unsubscribe(LocalDateTime.now());
    }

    @Override
    public boolean isSubscribed(Long memberId, Long projectId) {
        return subscriptionRepository.existsByMemberIdAndProjectIdAndDeletedAtIsNull(memberId, projectId);
    }

    @Override
    public List<MemberResponseDTO> getSubscribers(Long projectId) {
        List<Subscription> subscriptions = subscriptionRepository.findAllByProjectIdAndDeletedAtIsNull(projectId);
        return subscriptions.stream()
                .map(Subscription::getMember)
                .map(memberService::entityToDTO)
                .toList();
    }

    @Override
    public void notifySubscribersOnProjectUpdate(Project project) {
        List<Subscription> subscriptions = subscriptionRepository.findAllByProjectIdAndDeletedAtIsNull(project.getId());

        for (Subscription subscription : subscriptions) {
            Member member = subscription.getMember();
            MailSendRequestDTO dto = MailSendRequestDTO.builder()
                    .memberId(member.getId())
                    .email(member.getEmail())
                    .subject("[Pumati] 프로젝트가 업데이트되었습니다!")
                    .content(String.format(
                            "안녕하세요, %s님!\n\n구독 중인 프로젝트 '%s'가 업데이트되었습니다. 지금 확인해보세요!",
                            member.getNickname(), project.getTitle()))
                    .build();

            mailSendProducer.sendMail(dto);
        }
    }
}
