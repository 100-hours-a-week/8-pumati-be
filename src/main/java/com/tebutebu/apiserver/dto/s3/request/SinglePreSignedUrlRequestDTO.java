package com.tebutebu.apiserver.dto.s3.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SinglePreSignedUrlRequestDTO {

    @NotBlank(message = "파일 이름은 필수 입력 값입니다.")
    private String fileName;

    @NotBlank(message = "파일 타입은 필수 입력 값입니다.")
    private String contentType;

}
