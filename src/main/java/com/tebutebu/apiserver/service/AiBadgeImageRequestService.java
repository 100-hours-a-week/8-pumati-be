package com.tebutebu.apiserver.service;

import com.tebutebu.apiserver.dto.badge.request.MemberTeamBadgeUpdateRequestDTO;
import com.tebutebu.apiserver.dto.project.request.ProjectSummaryDTO;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Async;

@Transactional
public interface AiBadgeImageRequestService {

    @Async
    @Transactional(readOnly = true)
    void requestGenerateBadgeImage(ProjectSummaryDTO request);

    @Async
    @Transactional(readOnly = true)
    void requestUpdateBadgeImage(MemberTeamBadgeUpdateRequestDTO request);

}
