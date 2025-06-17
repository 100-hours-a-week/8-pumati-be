package com.tebutebu.apiserver.repository.paging.badge;

import com.tebutebu.apiserver.dto.ai.badge.response.TeamBadgeStatPageResponseDTO;
import com.tebutebu.apiserver.pagination.dto.request.ContextCountCursorPageRequestDTO;
import com.tebutebu.apiserver.pagination.internal.CursorPage;

public interface TeamBadgeStatPagingRepository {

    CursorPage<TeamBadgeStatPageResponseDTO> findByAcquiredCountCursor(ContextCountCursorPageRequestDTO req);

}
