package com.tebutebu.apiserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tebutebu.apiserver.dto.s3.request.MultiplePreSignedUrlsRequestDTO;
import com.tebutebu.apiserver.dto.s3.request.SinglePreSignedUrlRequestDTO;
import com.tebutebu.apiserver.fixture.s3.PreSignedUrlRequestDTOFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("PreSignedUrlController 통합 테스트")
class PreSignedUrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("단일 Pre-signed URL 생성")
    class SinglePreSignedUrl {

        @Test
        @DisplayName("단일 Pre-signed URL 생성 성공")
        void testGeneratePreSignedUrlSuccess() throws Exception {
            SinglePreSignedUrlRequestDTO requestDTO = PreSignedUrlRequestDTOFixture.createSingleRequestDTO("test.jpg", "image/jpeg");

            mockMvc.perform(post("/api/pre-signed-url")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.message").value("getPreSignedUrlSuccess"))
                    .andExpect(jsonPath("$.data.objectKey").exists())
                    .andExpect(jsonPath("$.data.uploadUrl").exists())
                    .andExpect(jsonPath("$.data.publicUrl").exists());
        }

        @Test
        @DisplayName("단일 URL 확장자 유효성 검증 실패")
        void generatePreSignedUrlInvalidExtension() throws Exception {
            SinglePreSignedUrlRequestDTO requestDTO = PreSignedUrlRequestDTOFixture.createSingleRequestDTO("test.gif", "image/gif");

            mockMvc.perform(post("/api/pre-signed-url")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("unsupportedFileExtension"));
        }
    }

    @Nested
    @DisplayName("다중 Pre-signed URL 생성")
    class MultiplePreSignedUrl {

        @Test
        @DisplayName("다중 Pre-signed URL 생성 성공")
        void createMultiplePreSignedUrlsSuccess() throws Exception {
            List<SinglePreSignedUrlRequestDTO> files = List.of(
                    PreSignedUrlRequestDTOFixture.createSingleRequestDTO("file1.jpg", "image/jpeg"),
                    PreSignedUrlRequestDTOFixture.createSingleRequestDTO("file2.png", "image/png")
            );

            MultiplePreSignedUrlsRequestDTO requestDTO = MultiplePreSignedUrlsRequestDTO.builder()
                    .files(files)
                    .build();

            mockMvc.perform(post("/api/pre-signed-urls")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.message").value("getPreSignedUrlsSuccess"))
                    .andExpect(jsonPath("$.data.urls", hasSize(2)));
        }

        @Test
        @DisplayName("다중 URL 요청 개수 초과")
        void createMultiplePreSignedUrlsCountExceeded() throws Exception {
            List<SinglePreSignedUrlRequestDTO> files = IntStream.rangeClosed(1, 20)
                    .mapToObj(i -> PreSignedUrlRequestDTOFixture.createSingleRequestDTO("file" + i + ".jpg", "image/jpeg"))
                    .toList();

            MultiplePreSignedUrlsRequestDTO requestDTO = MultiplePreSignedUrlsRequestDTO.builder()
                    .files(files)
                    .build();

            mockMvc.perform(post("/api/pre-signed-urls")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("requestCountExceeded"));
        }
    }

}
