package com.tebutebu.apiserver.dto.comment.response;

import com.tebutebu.apiserver.domain.enums.CommentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class CommentResponseDTO {

    private Long id;

    private Long projectId;

    private CommentType type;

    private String content;

    private AuthorDTO author;

    private LocalDateTime createdAt, modifiedAt;

}
