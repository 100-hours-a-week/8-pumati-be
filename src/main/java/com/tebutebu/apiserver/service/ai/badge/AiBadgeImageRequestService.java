package com.tebutebu.apiserver.service.ai.badge;

import com.tebutebu.apiserver.dto.ai.badge.request.TeamBadgeUpdateRequestDTO;
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
    void requestUpdateBadgeImage(TeamBadgeUpdateRequestDTO request);

}
