package com.tebutebu.apiserver.repository.paging.badge;

import com.tebutebu.apiserver.dto.badge.response.MemberTeamBadgePageResponseDTO;
import com.tebutebu.apiserver.pagination.dto.request.ContextCountCursorPageRequestDTO;
import com.tebutebu.apiserver.pagination.internal.CursorPage;

public interface MemberTeamBadgePagingRepository {

    CursorPage<MemberTeamBadgePageResponseDTO> findByAcquiredCountCursor(ContextCountCursorPageRequestDTO req);

}
