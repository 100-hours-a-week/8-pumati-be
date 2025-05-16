package com.tebutebu.apiserver.repository.paging.comment;

import com.tebutebu.apiserver.domain.Comment;
import com.tebutebu.apiserver.pagination.dto.request.CursorPageRequestDTO;
import com.tebutebu.apiserver.pagination.internal.CursorPage;

public interface CommentPagingRepository {

    CursorPage<Comment> findByProjectLatestCursor(Long projectId, CursorPageRequestDTO req);

}
