package com.tebutebu.apiserver.dto.s3.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SinglePreSignedUrlResponseDTO {

    private String objectKey;

    private String uploadUrl;

    private String publicUrl;

}
