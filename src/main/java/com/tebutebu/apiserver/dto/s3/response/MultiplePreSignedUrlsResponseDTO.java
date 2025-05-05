package com.tebutebu.apiserver.dto.s3.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MultiplePreSignedUrlsResponseDTO {

    private final List<SinglePreSignedUrlResponseDTO> urls;

}
