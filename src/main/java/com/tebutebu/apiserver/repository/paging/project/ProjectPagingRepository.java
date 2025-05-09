package com.tebutebu.apiserver.repository.paging.project;

import com.tebutebu.apiserver.domain.Project;
import com.tebutebu.apiserver.pagination.internal.CursorPage;
import com.tebutebu.apiserver.pagination.dto.request.CursorPageRequestDTO;

public interface ProjectPagingRepository {

    CursorPage<Project> findByRankingCursor(CursorPageRequestDTO req);

}
