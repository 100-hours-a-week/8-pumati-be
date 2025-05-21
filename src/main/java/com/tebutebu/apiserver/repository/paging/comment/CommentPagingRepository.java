package com.tebutebu.apiserver.repository.paging.comment;

import com.tebutebu.apiserver.dto.comment.response.CommentResponseDTO;
import com.tebutebu.apiserver.pagination.dto.request.CursorTimePageRequestDTO;
import com.tebutebu.apiserver.pagination.internal.CursorPage;

public interface CommentPagingRepository {

    CursorPage<CommentResponseDTO> findByProjectLatestCursor(Long projectId, CursorTimePageRequestDTO req);

}
