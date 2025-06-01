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
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
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
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("PreSignedUrlServiceImpl Unit Tests")
class PreSignedUrlServiceTest {

    @InjectMocks
    private PreSignedUrlServiceImpl preSignedUrlService;

    @Mock
    private S3Presigner s3Presigner;

    @Mock
    private PresignedPutObjectRequest presignedPutObjectRequest;

    @Mock
    private URL mockUrl;

    private final String bucketName = "test-bucket";

    private final String region = "ap-northeast-2";

    private final long putExpirationMinutes = 15L;

    private final int maxCount = 10;

    @BeforeEach
    void setUp() {
        ReflectionTestUtil.setPrivateField(preSignedUrlService, "bucketName", bucketName);
        ReflectionTestUtil.setPrivateField(preSignedUrlService, "region", region);
        ReflectionTestUtil.setPrivateField(preSignedUrlService, "putExpirationMinutes", putExpirationMinutes);
        ReflectionTestUtil.setPrivateField(preSignedUrlService, "maxCount", maxCount);

        when(s3Presigner.presignPutObject(any(Consumer.class))).thenReturn(presignedPutObjectRequest);
        when(presignedPutObjectRequest.url()).thenReturn(mockUrl);
        when(mockUrl.toString()).thenReturn("https://s3.test/upload-url");
    }

    @Nested
    class GenerateSingleUrlTests {

        @Test
        @DisplayName("단일 Pre-signed URL 생성 성공")
        void testGeneratePreSignedUrlSuccess() {
            SinglePreSignedUrlRequestDTO singlePreSignedUrlRequestDTO = SinglePreSignedUrlRequestDTO.builder()
                    .fileName("test.jpg")
                    .contentType("image/jpeg")
                    .build();

            SinglePreSignedUrlResponseDTO singlePreSignedUrlResponseDTO = preSignedUrlService.generatePreSignedUrl(singlePreSignedUrlRequestDTO);

            assertNotNull(singlePreSignedUrlResponseDTO);
            assertTrue(singlePreSignedUrlResponseDTO.getObjectKey().endsWith(".jpg"));
            assertTrue(singlePreSignedUrlResponseDTO.getUploadUrl().contains("https://s3.test/upload-url"));
            assertTrue(singlePreSignedUrlResponseDTO.getPublicUrl().contains("https://test-bucket.s3.ap-northeast-2.amazonaws.com/"));
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
            List<SinglePreSignedUrlRequestDTO> files = IntStream.rangeClosed(1, maxCount)
                    .mapToObj(i -> SinglePreSignedUrlRequestDTO.builder()
                            .fileName("file" + i + ".jpg")
                            .contentType("image/jpeg")
                            .build())
                    .collect(Collectors.toList());
            MultiplePreSignedUrlsRequestDTO multiplePreSignedUrlsRequestDTO = MultiplePreSignedUrlsRequestDTO.builder().files(files).build();

            MultiplePreSignedUrlsResponseDTO multiplePreSignedUrlsResponseDTO = preSignedUrlService.generatePreSignedUrls(multiplePreSignedUrlsRequestDTO);

            assertNotNull(multiplePreSignedUrlsResponseDTO);
            assertEquals(2, multiplePreSignedUrlsResponseDTO.getUrls().size());
            multiplePreSignedUrlsResponseDTO.getUrls().forEach(url -> {
                assertNotNull(url.getObjectKey());
                assertNotNull(url.getUploadUrl());
                assertNotNull(url.getPublicUrl());
            });
        }

        @Test
        @DisplayName("파일 리스트 개수 초과 예외")
        void testGenerateMultiplePreSignedUrlsRequestCountExceeded() {
            List<SinglePreSignedUrlRequestDTO> files = IntStream.rangeClosed(1, maxCount + 1)
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
