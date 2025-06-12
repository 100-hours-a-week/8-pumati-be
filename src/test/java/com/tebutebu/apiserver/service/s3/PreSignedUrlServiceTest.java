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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
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

    private static final String ALLOWED_EXTENSIONS_RAW = ".jpg,.jpeg,.png";

    @BeforeEach
    void setUp() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        ReflectionTestUtil.setPrivateField(preSignedUrlService, "bucketName", BUCKET_NAME);
        ReflectionTestUtil.setPrivateField(preSignedUrlService, "region", REGION);
        ReflectionTestUtil.setPrivateField(preSignedUrlService, "putExpirationMinutes", PUT_EXPIRATION_MINUTES);
        ReflectionTestUtil.setPrivateField(preSignedUrlService, "maxCount", MAX_COUNT);

        ReflectionTestUtil.setPrivateField(preSignedUrlService, "allowedExtensionsRaw", ALLOWED_EXTENSIONS_RAW);
        Method initMethod = PreSignedUrlServiceImpl.class.getDeclaredMethod("initAllowedExtensions");
        initMethod.setAccessible(true);
        initMethod.invoke(preSignedUrlService);
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
    @DisplayName("단일 Pre-Signed URL 생성 검증")
    class GenerateSingleUrlTests {

        @Test
        @DisplayName("단일 Pre-signed URL 생성 성공")
        void testGeneratePreSignedUrlSuccess() {
            setupS3PreSignerMock();

            SinglePreSignedUrlRequestDTO requestDTO = SinglePreSignedUrlRequestDTO.builder()
                    .fileName("test.jpg")
                    .contentType("image/jpeg")
                    .build();

            SinglePreSignedUrlResponseDTO response = preSignedUrlService.generatePreSignedUrl(requestDTO);

            assertNotNull(response);
            assertTrue(response.getObjectKey().endsWith(".jpg"));
            assertTrue(response.getUploadUrl().contains(UPLOAD_URL));
            assertTrue(response.getPublicUrl().contains(PUBLIC_URL_PREFIX));
        }

        @Test
        @DisplayName("파일명 확장자 누락 예외")
        void testGeneratePreSignedUrlInvalidExtension() {
            SinglePreSignedUrlRequestDTO requestDTO = SinglePreSignedUrlRequestDTO.builder()
                    .fileName("invalidFile")
                    .contentType("image/jpeg")
                    .build();

            CustomValidationException e = assertThrows(CustomValidationException.class,
                    () -> preSignedUrlService.generatePreSignedUrl(requestDTO));
            assertEquals("invalidFileExtension", e.getMessage());
        }
    }

    @Nested
    @DisplayName("다중 Pre-Signed URL 생성 검증")
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

            MultiplePreSignedUrlsRequestDTO requestDTO = MultiplePreSignedUrlsRequestDTO.builder().files(files).build();

            MultiplePreSignedUrlsResponseDTO response = preSignedUrlService.generatePreSignedUrls(requestDTO);

            assertNotNull(response);
            assertEquals(MAX_COUNT, response.getUrls().size());
            response.getUrls().forEach(url -> {
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

            MultiplePreSignedUrlsRequestDTO requestDTO = MultiplePreSignedUrlsRequestDTO.builder().files(files).build();

            CustomValidationException e = assertThrows(CustomValidationException.class,
                    () -> preSignedUrlService.generatePreSignedUrls(requestDTO));
            assertEquals("requestCountExceeded", e.getMessage());
        }
    }

    @Nested
    @DisplayName("파일 확장자 검증")
    class FileExtensionValidationTests {

        @ParameterizedTest(name = "유효한 확장자: {0}")
        @CsvSource({
                "file.jpg, image/jpeg",
                "file.jpeg, image/jpeg",
                "file.png, image/png",
                "file.JPG, image/jpeg",
                "file.PNG, image/png"
        })
        void testValidExtensions(String fileName, String contentType) {
            setupS3PreSignerMock();

            SinglePreSignedUrlRequestDTO dto = SinglePreSignedUrlRequestDTO.builder()
                    .fileName(fileName)
                    .contentType(contentType)
                    .build();

            assertDoesNotThrow(() -> preSignedUrlService.generatePreSignedUrl(dto));
        }

        @ParameterizedTest(name = "유효하지 않은 확장자: {0}")
        @ValueSource(strings = {"file", "image", ".hidden", "invalidate.", "", " "})
        void testInvalidExtensions(String fileName) {
            SinglePreSignedUrlRequestDTO dto = SinglePreSignedUrlRequestDTO.builder()
                    .fileName(fileName)
                    .contentType("image/jpeg")
                    .build();

            assertThrows(CustomValidationException.class,
                    () -> preSignedUrlService.generatePreSignedUrl(dto));
        }
    }

    @Nested
    @DisplayName("S3PreSigner 예외 상황")
    class S3PreSignerExceptionTests {

        @Test
        @DisplayName("S3 PreSigner 내부 예외 발생 시")
        void testPreSignUrlS3ExceptionThrown() {
            when(s3Presigner.presignPutObject(any(Consumer.class)))
                    .thenThrow(S3Exception.builder().message("S3 error").statusCode(500).build());

            SinglePreSignedUrlRequestDTO requestDTO = SinglePreSignedUrlRequestDTO.builder()
                    .fileName("test.jpg")
                    .contentType("image/jpeg")
                    .build();
            assertThrows(S3Exception.class, () -> preSignedUrlService.generatePreSignedUrl(requestDTO));
        }
    }

}
