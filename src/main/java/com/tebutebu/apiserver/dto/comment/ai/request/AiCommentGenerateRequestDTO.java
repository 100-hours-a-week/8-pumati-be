package com.tebutebu.apiserver.dto.comment.ai.request;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiCommentGenerateRequestDTO {

    private String commentType;

    private ProjectSummaryDTO projectSummary;

}
