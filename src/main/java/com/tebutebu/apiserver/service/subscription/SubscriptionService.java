package com.tebutebu.apiserver.service.subscription;

import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface SubscriptionService {

    Long subscribe(Long memberId, Long projectId);

    void unsubscribe(Long memberId, Long projectId);

    @Transactional(readOnly = true)
    boolean isSubscribed(Long memberId, Long projectId);

}
