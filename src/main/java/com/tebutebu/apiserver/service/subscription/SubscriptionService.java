package com.tebutebu.apiserver.service.subscription;

import com.tebutebu.apiserver.domain.Project;
import com.tebutebu.apiserver.dto.member.response.MemberResponseDTO;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface SubscriptionService {

    Long subscribe(Long memberId, Long projectId);

    void unsubscribe(Long memberId, Long projectId);

    @Transactional(readOnly = true)
    boolean isSubscribed(Long memberId, Long projectId);

    @Transactional(readOnly = true)
    List<MemberResponseDTO> getSubscribers(Long projectId);

    @Async
    @Transactional(readOnly = true)
    void notifySubscribersOnProjectUpdate(Project project);

}
