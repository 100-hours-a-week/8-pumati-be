package com.tebutebu.apiserver.service.ai.comment;

import com.tebutebu.apiserver.dto.ai.comment.request.AiCommentGenerateRequestDTO;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface AiCommentRequestService {

    @Async
    @Transactional(readOnly = true)
    void requestAiComment(Long projectId, AiCommentGenerateRequestDTO request);

}
