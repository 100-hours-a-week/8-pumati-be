package com.tebutebu.apiserver.repository.paging.badge;

import com.tebutebu.apiserver.dto.ai.badge.response.TeamBadgePageResponseDTO;
import com.tebutebu.apiserver.pagination.dto.request.ContextCountCursorPageRequestDTO;
import com.tebutebu.apiserver.pagination.internal.CursorPage;

public interface TeamBadgePagingRepository {

    CursorPage<TeamBadgePageResponseDTO> findByAcquiredCountCursor(ContextCountCursorPageRequestDTO req);

}
