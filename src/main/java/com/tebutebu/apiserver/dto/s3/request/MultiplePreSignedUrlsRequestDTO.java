package com.tebutebu.apiserver.dto.s3.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultiplePreSignedUrlsRequestDTO {

    @NotEmpty(message = "파일 리스트는 필수 입력 값입니다.")
    @Size(min = 1, max = 10, message = "requestCountExceeded")
    private List<@Valid SinglePreSignedUrlRequestDTO> files;

}
