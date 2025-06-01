package com.tebutebu.apiserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tebutebu.apiserver.config.CustomSecurityConfig;
import com.tebutebu.apiserver.dto.s3.request.MultiplePreSignedUrlsRequestDTO;
import com.tebutebu.apiserver.dto.s3.request.SinglePreSignedUrlRequestDTO;
import com.tebutebu.apiserver.dto.s3.response.MultiplePreSignedUrlsResponseDTO;
import com.tebutebu.apiserver.dto.s3.response.SinglePreSignedUrlResponseDTO;
import com.tebutebu.apiserver.security.handler.CustomLoginFailHandler;
import com.tebutebu.apiserver.security.handler.CustomLoginSuccessHandler;
import com.tebutebu.apiserver.security.handler.CustomLogoutHandler;
import com.tebutebu.apiserver.security.service.CustomOAuth2UserService;
import com.tebutebu.apiserver.service.s3.PreSignedUrlServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(PreSignedUrlController.class)
@Import(CustomSecurityConfig.class)
@DisplayName("PreSignedUrlController Unit Tests")
class PreSignedUrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PreSignedUrlServiceImpl preSignedUrlService;

    @MockitoBean
    private CustomOAuth2UserService customOAuth2UserService;

    @MockitoBean
    private CustomLoginSuccessHandler customLoginSuccessHandler;

    @MockitoBean
    private CustomLoginFailHandler customLoginFailHandler;

    @MockitoBean
    private CustomLogoutHandler customLogoutHandler;

    @Autowired
    private ObjectMapper objectMapper;

    private final int fileCount = 10;

    @Nested
    class SinglePreSignedUrlTests {

        @Test
        @DisplayName("단일 Pre-signed URL 생성 성공")
        void testGeneratePreSignedUrlSuccess() throws Exception {
            SinglePreSignedUrlRequestDTO requestDTO = SinglePreSignedUrlRequestDTO.builder()
                    .fileName("test.jpg")
                    .contentType("image/jpeg")
                    .build();

            SinglePreSignedUrlResponseDTO responseDTO = SinglePreSignedUrlResponseDTO.builder()
                    .objectKey("uploads/test.jpg")
                    .uploadUrl("https://s3.test/upload-url")
                    .publicUrl("https://s3.test/public-url")
                    .build();

            Mockito.when(preSignedUrlService.generatePreSignedUrl(any(SinglePreSignedUrlRequestDTO.class)))
                    .thenReturn(responseDTO);

            mockMvc.perform(post("/api/pre-signed-url")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.message").value("getPreSignedUrlSuccess"))
                    .andExpect(jsonPath("$.data.objectKey").value("uploads/test.jpg"))
                    .andExpect(jsonPath("$.data.uploadUrl").value("https://s3.test/upload-url"))
                    .andExpect(jsonPath("$.data.publicUrl").value("https://s3.test/public-url"));
        }

        @Test
        @DisplayName("파일명이 비어 있을 경우 실패")
        void testGeneratePreSignedUrlEmptyFileName() throws Exception {
            SinglePreSignedUrlRequestDTO requestDTO = SinglePreSignedUrlRequestDTO.builder()
                    .fileName("")
                    .contentType("image/jpeg")
                    .build();

            mockMvc.perform(post("/api/pre-signed-url")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("파일 이름은 필수 입력 값입니다."));
        }
    }

    @Nested
    class MultiplePreSignedUrlsTests {

        @Test
        @DisplayName("다중 Pre-signed URL 생성 성공")
        void testGenerateMultiplePreSignedUrlsSuccess() throws Exception {
            MultiplePreSignedUrlsRequestDTO requestDTO = MultiplePreSignedUrlsRequestDTO.builder()
                    .files(IntStream.rangeClosed(1, fileCount)
                            .mapToObj(i -> SinglePreSignedUrlRequestDTO.builder()
                                    .fileName("file" + i + ".jpg")
                                    .contentType("image/jpeg")
                                    .build())
                            .toList())
                    .build();

            MultiplePreSignedUrlsResponseDTO responseDTO = MultiplePreSignedUrlsResponseDTO.builder()
                    .urls(IntStream.rangeClosed(1, fileCount)
                            .mapToObj(i -> SinglePreSignedUrlResponseDTO.builder()
                                    .objectKey("uploads/file" + i + ".jpg")
                                    .uploadUrl("https://s3.test/upload-url" + i)
                                    .publicUrl("https://s3.test/public-url" + i)
                                    .build())
                            .toList())
                    .build();

            Mockito.when(preSignedUrlService.generatePreSignedUrls(any(MultiplePreSignedUrlsRequestDTO.class)))
                    .thenReturn(responseDTO);

            mockMvc.perform(post("/api/pre-signed-urls")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.message").value("getPreSignedUrlsSuccess"))
                    .andExpect(jsonPath("$.data.urls[0].objectKey").value("uploads/file1.jpg"))
                    .andExpect(jsonPath("$.data.urls[1].objectKey").value("uploads/file2.jpg"));
        }

        @Test
        @DisplayName("파일 리스트 비어 있을 경우 실패")
        void testEmptyFileList() throws Exception {
            MultiplePreSignedUrlsRequestDTO requestDTO = MultiplePreSignedUrlsRequestDTO.builder()
                    .files(List.of())
                    .build();

            mockMvc.perform(post("/api/pre-signed-urls")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("요청 가능한 파일 개수는 1~10개 입니다."));
        }

        @Test
        @DisplayName("파일 개수 초과 시 실패")
        void testExceedFileListCount() throws Exception {
            List<SinglePreSignedUrlRequestDTO> files = IntStream.rangeClosed(1, fileCount + 1)
                    .mapToObj(i -> SinglePreSignedUrlRequestDTO.builder()
                            .fileName("file" + i + ".jpg")
                            .contentType("image/jpeg")
                            .build())
                    .toList();

            MultiplePreSignedUrlsRequestDTO requestDTO = MultiplePreSignedUrlsRequestDTO.builder()
                    .files(files)
                    .build();

            mockMvc.perform(post("/api/pre-signed-urls")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("요청 가능한 파일 개수는 1~10개 입니다."));
        }
    }

}
