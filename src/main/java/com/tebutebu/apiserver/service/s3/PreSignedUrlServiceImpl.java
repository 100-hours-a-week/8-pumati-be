package com.tebutebu.apiserver.service.s3;

import com.tebutebu.apiserver.dto.s3.request.MultiplePreSignedUrlsRequestDTO;
import com.tebutebu.apiserver.dto.s3.request.SinglePreSignedUrlRequestDTO;
import com.tebutebu.apiserver.dto.s3.response.MultiplePreSignedUrlsResponseDTO;
import com.tebutebu.apiserver.dto.s3.response.SinglePreSignedUrlResponseDTO;
import com.tebutebu.apiserver.util.exception.CustomValidationException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PreSignedUrlServiceImpl implements PreSignedUrlService {

    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    @Value("${aws.region}")
    private String region;

    @Value("${aws.s3.expiration.put.minutes:15}")
    private long putExpirationMinutes;

    @Value("${aws.s3.max-request-count:10}")
    private int maxCount;

    @Value("${aws.s3.allowed-extensions}")
    private String allowedExtensionsRaw;

    private Set<String> allowedExtensions;

    @PostConstruct
    private void initAllowedExtensions() {
        this.allowedExtensions = Arrays.stream(allowedExtensionsRaw.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    @Override
    public SinglePreSignedUrlResponseDTO generatePreSignedUrl(SinglePreSignedUrlRequestDTO dto) {
        String fileName = dto.getFileName().trim();
        String ext = extractExtension(fileName);
        validateExtension(ext);

        String objectKey = "uploads/" + UUID.randomUUID() + ext;

        String uploadUrl = generatePutPreSignedUrl(objectKey, dto.getContentType());
        String publicUrl = String.format(
                "https://%s.s3.%s.amazonaws.com/%s",
                bucketName, region, objectKey
        );

        return SinglePreSignedUrlResponseDTO.builder()
                .objectKey(objectKey)
                .uploadUrl(uploadUrl)
                .publicUrl(publicUrl)
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
                .acl(ObjectCannedACL.PUBLIC_READ)
                .build();
        PresignedPutObjectRequest preSignedPut = s3Presigner.presignPutObject(r -> r
                .signatureDuration(Duration.ofMinutes(putExpirationMinutes))
                .putObjectRequest(putReq)
        );
        return preSignedPut.url().toString();
    }

    private String extractExtension(String fileName) {
        int idx = fileName.lastIndexOf('.');
        if (idx <= 0 || idx == fileName.length() - 1) {
            throw new CustomValidationException("invalidFileExtension");
        }
        return fileName.substring(idx).toLowerCase();
    }

    private void validateExtension(String ext) {
        if (!allowedExtensions.contains(ext)) {
            throw new CustomValidationException("unsupportedFileExtension");
        }
    }

}
