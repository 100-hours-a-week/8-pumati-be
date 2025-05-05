package com.tebutebu.apiserver.service;

import com.tebutebu.apiserver.dto.s3.request.MultiplePreSignedUrlsRequestDTO;
import com.tebutebu.apiserver.dto.s3.request.SinglePreSignedUrlRequestDTO;
import com.tebutebu.apiserver.dto.s3.response.MultiplePreSignedUrlsResponseDTO;
import com.tebutebu.apiserver.dto.s3.response.SinglePreSignedUrlResponseDTO;
import com.tebutebu.apiserver.util.exception.CustomValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PreSignedUrlServiceImpl implements PreSignedUrlService {

    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    @Value("${aws.s3.expiration.put.minutes:15}")
    private long putExpirationMinutes;

    @Value("${aws.s3.expiration.get.hours:24}")
    private long getExpirationHours;

    @Value("${aws.s3.max-request-count:10}")
    private int maxCount;

    @Override
    public SinglePreSignedUrlResponseDTO generatePreSignedUrl(SinglePreSignedUrlRequestDTO dto) {
        String fileName = dto.getFileName().trim();
        int idx = fileName.lastIndexOf('.');
        if (idx <= 0) {
            throw new CustomValidationException("invalidFileExtension");
        }
        String ext = fileName.substring(idx);
        String objectKey = "uploads/" + UUID.randomUUID() + ext;

        String uploadUrl = generatePutPreSignedUrl(objectKey, dto.getContentType());
        String downloadUrl = generateGetPreSignedUrl(objectKey);

        return SinglePreSignedUrlResponseDTO.builder()
                .objectKey(objectKey)
                .uploadUrl(uploadUrl)
                .downloadUrl(downloadUrl)
                .build();
    }

    @Override
    public MultiplePreSignedUrlsResponseDTO generatePreSignedUrls(MultiplePreSignedUrlsRequestDTO dto) {
        List<SinglePreSignedUrlRequestDTO> files = dto.getFiles();
        if (files.isEmpty() || files.size() > maxCount) {
            throw new CustomValidationException("requestCountExceeded");
        }
        List<SinglePreSignedUrlResponseDTO> urls = files.stream()
                .map(this::generatePreSignedUrl)
                .collect(Collectors.toList());
        return MultiplePreSignedUrlsResponseDTO.builder()
                .urls(urls)
                .build();
    }

    private String generatePutPreSignedUrl(String objectKey, String contentType) {
        PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType(contentType)
                .build();
        PresignedPutObjectRequest preSignedPut = s3Presigner.presignPutObject(r -> r
                .signatureDuration(Duration.ofMinutes(putExpirationMinutes))
                .putObjectRequest(putReq)
        );
        return preSignedPut.url().toString();
    }

    private String generateGetPreSignedUrl(String objectKey) {
        GetObjectRequest getReq = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();
        PresignedGetObjectRequest preSignedGet = s3Presigner.presignGetObject(r -> r
                .signatureDuration(Duration.ofHours(getExpirationHours))
                .getObjectRequest(getReq)
        );
        return preSignedGet.url().toString();
    }

}
