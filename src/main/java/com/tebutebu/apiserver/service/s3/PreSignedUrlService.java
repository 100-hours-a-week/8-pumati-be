package com.tebutebu.apiserver.service.s3;

import com.tebutebu.apiserver.dto.s3.request.MultiplePreSignedUrlsRequestDTO;
import com.tebutebu.apiserver.dto.s3.request.SinglePreSignedUrlRequestDTO;
import com.tebutebu.apiserver.dto.s3.response.MultiplePreSignedUrlsResponseDTO;
import com.tebutebu.apiserver.dto.s3.response.SinglePreSignedUrlResponseDTO;

public interface PreSignedUrlService {

    SinglePreSignedUrlResponseDTO  generatePreSignedUrl(SinglePreSignedUrlRequestDTO dto);

    MultiplePreSignedUrlsResponseDTO generatePreSignedUrls(MultiplePreSignedUrlsRequestDTO dto);

}
