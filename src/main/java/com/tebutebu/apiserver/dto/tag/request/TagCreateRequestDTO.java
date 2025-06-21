package com.tebutebu.apiserver.dto.tag.request;

import com.tebutebu.apiserver.global.constant.ValidationMessages;
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

    @NotBlank(message = ValidationMessages.TAG_CONTENT_REQUIRED)
    @Pattern(regexp = "^\\S{2,20}$", message = ValidationMessages.TAG_CONTENT_VIOLATED)
    @Size(min = 2, max = 20, message = ValidationMessages.TAG_CONTENT_MAX_LENGTH_EXCEEDED)
    private String content;

}
