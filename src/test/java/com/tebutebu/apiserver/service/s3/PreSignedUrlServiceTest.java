package com.tebutebu.apiserver.service.s3;

import com.tebutebu.apiserver.dto.s3.request.MultiplePreSignedUrlsRequestDTO;
import com.tebutebu.apiserver.dto.s3.request.SinglePreSignedUrlRequestDTO;
import com.tebutebu.apiserver.dto.s3.response.MultiplePreSignedUrlsResponseDTO;
import com.tebutebu.apiserver.dto.s3.response.SinglePreSignedUrlResponseDTO;
import com.tebutebu.apiserver.util.ReflectionTestUtil;
import com.tebutebu.apiserver.util.exception.CustomValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.net.URL;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PreSignedUrlServiceImpl Unit Tests")
class PreSignedUrlServiceTest {

    @InjectMocks
    private PreSignedUrlServiceImpl preSignedUrlService;

    @Mock
    private S3Presigner s3Presigner;

    private static final String UPLOAD_URL = "https://s3.test/upload-url";

    private static final String BUCKET_NAME = "test-bucket";

    private static final String REGION = "ap-northeast-2";

    private static final long PUT_EXPIRATION_MINUTES = 15L;

    private static final int MAX_COUNT = 10;

    private static final String PUBLIC_URL_PREFIX = "https://" + BUCKET_NAME + ".s3." + REGION + ".amazonaws.com/";

    @BeforeEach
    void setUp() {
        ReflectionTestUtil.setPrivateField(preSignedUrlService, "bucketName", BUCKET_NAME);
        ReflectionTestUtil.setPrivateField(preSignedUrlService, "region", REGION);
        ReflectionTestUtil.setPrivateField(preSignedUrlService, "putExpirationMinutes", PUT_EXPIRATION_MINUTES);
        ReflectionTestUtil.setPrivateField(preSignedUrlService, "maxCount", MAX_COUNT);
    }

    private void setupS3PreSignerMock() {
        when(s3Presigner.presignPutObject(any(Consumer.class)))
                .thenAnswer(invocation -> {
                    PresignedPutObjectRequest presignedPutObjectRequest = Mockito.mock(
                            PresignedPutObjectRequest.class,
                            Mockito.withSettings().defaultAnswer(invocationOnMock -> {
                                if ("url".equals(invocationOnMock.getMethod().getName())) {
                                    return new URL(UPLOAD_URL);
                                }
                                return invocationOnMock.callRealMethod();
                            })
                    );
                    return presignedPutObjectRequest;
                });
    }

    @Nested
    class GenerateSingleUrlTests {

        @Test
        @DisplayName("단일 Pre-signed URL 생성 성공")
        void testGeneratePreSignedUrlSuccess() {
            setupS3PreSignerMock();

            SinglePreSignedUrlRequestDTO singlePreSignedUrlRequestDTO = SinglePreSignedUrlRequestDTO.builder()
                    .fileName("test.jpg")
                    .contentType("image/jpeg")
                    .build();

            SinglePreSignedUrlResponseDTO singlePreSignedUrlResponseDTO = preSignedUrlService.generatePreSignedUrl(singlePreSignedUrlRequestDTO);

            assertNotNull(singlePreSignedUrlResponseDTO);
            assertTrue(singlePreSignedUrlResponseDTO.getObjectKey().endsWith(".jpg"));
            assertTrue(singlePreSignedUrlResponseDTO.getUploadUrl().contains(UPLOAD_URL));
            assertTrue(singlePreSignedUrlResponseDTO.getPublicUrl().contains(PUBLIC_URL_PREFIX));
        }

        @Test
        @DisplayName("파일명 확장자 누락 예외")
        void testGeneratePreSignedUrlInvalidExtension() {
            SinglePreSignedUrlRequestDTO singlePreSignedUrlRequestDTO = SinglePreSignedUrlRequestDTO.builder()
                    .fileName("invalidFile")
                    .contentType("image/jpeg")
                    .build();

            CustomValidationException e = assertThrows(CustomValidationException.class,
                    () -> preSignedUrlService.generatePreSignedUrl(singlePreSignedUrlRequestDTO));
            assertEquals("invalidFileExtension", e.getMessage());
        }
    }

    @Nested
    class GenerateMultipleUrlsTests {

        @Test
        @DisplayName("다중 Pre-signed URL 생성 성공")
        void testGenerateMultiplePreSignedUrlsSuccess() {
            setupS3PreSignerMock();

            List<SinglePreSignedUrlRequestDTO> files = IntStream.rangeClosed(1, MAX_COUNT)
                    .mapToObj(i -> SinglePreSignedUrlRequestDTO.builder()
                            .fileName("file" + i + ".jpg")
                            .contentType("image/jpeg")
                            .build())
                    .collect(Collectors.toList());

            MultiplePreSignedUrlsRequestDTO multiplePreSignedUrlsRequestDTO = MultiplePreSignedUrlsRequestDTO.builder().files(files).build();

            MultiplePreSignedUrlsResponseDTO multiplePreSignedUrlsResponseDTO = preSignedUrlService.generatePreSignedUrls(multiplePreSignedUrlsRequestDTO);

            assertNotNull(multiplePreSignedUrlsResponseDTO);
            assertEquals(MAX_COUNT, multiplePreSignedUrlsResponseDTO.getUrls().size());
            multiplePreSignedUrlsResponseDTO.getUrls().forEach(url -> {
                assertNotNull(url.getObjectKey());
                assertNotNull(url.getUploadUrl());
                assertNotNull(url.getPublicUrl());
            });
        }

        @Test
        @DisplayName("파일 리스트 개수 초과 예외")
        void testGenerateMultiplePreSignedUrlsRequestCountExceeded() {
            List<SinglePreSignedUrlRequestDTO> files = IntStream.rangeClosed(1, MAX_COUNT + 1)
                    .mapToObj(i -> SinglePreSignedUrlRequestDTO.builder()
                            .fileName("file" + i + ".jpg")
                            .contentType("image/jpeg")
                            .build())
                    .collect(Collectors.toList());

            MultiplePreSignedUrlsRequestDTO multiplePreSignedUrlsRequestDTO = MultiplePreSignedUrlsRequestDTO.builder().files(files).build();

            CustomValidationException e = assertThrows(CustomValidationException.class,
                    () -> preSignedUrlService.generatePreSignedUrls(multiplePreSignedUrlsRequestDTO));
            assertEquals("requestCountExceeded", e.getMessage());
        }
    }
}
