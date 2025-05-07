package com.tebutebu.apiserver.dto.tag.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
public class TagCreateRequestDTO {

    @NotBlank(message = "태그는 필수 입력 값입니다.")
    @Pattern(regexp = "^\\S{2,20}$", message = "태그 내용은 공백을 포함할 수 없으며 2자 이상 20자 이하여야 합니다.")
    @Size(min = 2, max = 20, message = "태그는 2자 이상 20자 이하여야 합니다.")
    private String content;

}
