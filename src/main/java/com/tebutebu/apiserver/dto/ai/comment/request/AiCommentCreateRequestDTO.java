package com.tebutebu.apiserver.dto.ai.comment.request;

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
@NoArgsConstructor
@AllArgsConstructor
public class AiCommentCreateRequestDTO {

    @NotBlank(message = "댓글 내용은 필수 입력 값입니다.")
    @Size(max = 300, message = "댓글 내용은 최대 300자까지 가능합니다.")
    private String content;

    @NotBlank(message = "작성자 이름은 필수 입력 값입니다.")
    private String authorName;

    @NotBlank(message = "작성자 닉네임은 필수 입력 값입니다.")
    private String authorNickname;

}
