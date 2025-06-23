package com.tebutebu.apiserver.dto.s3.request;

import com.tebutebu.apiserver.global.constant.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
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
public class SinglePreSignedUrlRequestDTO {

    @NotBlank(message = ValidationMessages.FILE_NAME_REQUIRED)
    private String fileName;

    @NotBlank(message = ValidationMessages.CONTENT_TYPE_REQUIRED)
    private String contentType;

}
