package com.tebutebu.apiserver.dto.comment.request;

import com.tebutebu.apiserver.global.constant.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentUpdateRequestDTO {

    @NotBlank(message = ValidationMessages.COMMENT_CONTENT_REQUIRED)
    @Size(max = 300, message = ValidationMessages.COMMENT_CONTENT_MAX_LENGTH_EXCEEDED)
    private String content;

}
