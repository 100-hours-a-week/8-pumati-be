package com.tebutebu.apiserver.repository.paging.project;

import com.tebutebu.apiserver.dto.project.response.ProjectPageResponseDTO;
import com.tebutebu.apiserver.pagination.dto.request.ContextCursorPageRequestDTO;
import com.tebutebu.apiserver.pagination.dto.request.CursorTimePageRequestDTO;
import com.tebutebu.apiserver.pagination.internal.CursorPage;

public interface ProjectPagingRepository {

    CursorPage<ProjectPageResponseDTO> findByRankingCursor(ContextCursorPageRequestDTO req);

    CursorPage<ProjectPageResponseDTO> findByLatestCursor(CursorTimePageRequestDTO req);

}
