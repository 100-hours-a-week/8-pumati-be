package com.tebutebu.apiserver.repository.paging.project;

import com.tebutebu.apiserver.dto.project.response.ProjectPageResponseDTO;
import com.tebutebu.apiserver.pagination.internal.CursorPage;
import com.tebutebu.apiserver.pagination.dto.request.CursorPageRequestDTO;

public interface ProjectPagingRepository {

    CursorPage<ProjectPageResponseDTO> findByRankingCursor(CursorPageRequestDTO req);

    CursorPage<ProjectPageResponseDTO> findByLatestCursor(CursorPageRequestDTO req);

}
