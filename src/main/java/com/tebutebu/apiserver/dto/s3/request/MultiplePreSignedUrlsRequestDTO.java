package com.tebutebu.apiserver.dto.s3.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MultiplePreSignedUrlsRequestDTO {

    @NotEmpty(message = "파일 리스트는 필수 입력 값입니다.")
    @Size(min = 1, max = 10, message = "요청 가능한 파일 개수는 1~10개 입니다.")
    private List<@Valid SinglePreSignedUrlRequestDTO> files;

}
