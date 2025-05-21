package com.tebutebu.apiserver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tebutebu.apiserver.dto.comment.ai.request.AiCommentGenerateRequestDTO;
import com.tebutebu.apiserver.dto.comment.ai.response.AiCommentGenerateResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Service
@Log4j2
@RequiredArgsConstructor
public class AiCommentRequestServiceImpl implements AiCommentRequestService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ai.comment.service.url}")
    private String aiCommentServiceUrl;

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    public void requestAiComment(Long projectId, AiCommentGenerateRequestDTO request) {
        try {
            String jsonBody = mapper.writeValueAsString(request);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> httpEntity = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<AiCommentGenerateResponseDTO> response = restTemplate.exchange(
                    aiCommentServiceUrl + "/api/projects/" + projectId + "/comments",
                    HttpMethod.POST,
                    httpEntity,
                    AiCommentGenerateResponseDTO.class
            );

            if (response.getStatusCode().is2xxSuccessful() && Objects.requireNonNull(response.getBody()).getData() != null) {
                log.info(response.getBody());
            } else {
                log.warn("AI comment server responded with non-2xx: {}, body: {}", response.getStatusCode(), response.getBody());
            }

        } catch (Exception e) {
            log.warn("Error calling AI comment service: {}", e.getMessage());
        }
    }

}
