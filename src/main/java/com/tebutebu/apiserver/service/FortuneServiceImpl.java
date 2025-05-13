package com.tebutebu.apiserver.service;

import com.tebutebu.apiserver.dto.fortune.response.DevLuckDTO;
import com.tebutebu.apiserver.dto.fortune.request.FortuneGenerateRequestDTO;
import com.tebutebu.apiserver.dto.fortune.response.FortuneResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Service
@Log4j2
@RequiredArgsConstructor
public class FortuneServiceImpl implements FortuneService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${fortune.service.url}")
    private String fortuneServiceUrl;

    @Value("${fortune.service.error-message}")
    private String fortuneServiceErrorMessage;

    @Override
    public DevLuckDTO getnerateDevLuck(FortuneGenerateRequestDTO request) {
        try {
            HttpEntity<FortuneGenerateRequestDTO> httpEntity = new HttpEntity<>(request);
            ResponseEntity<FortuneResponseDTO> response = restTemplate.postForEntity(
                    fortuneServiceUrl + "/api/llm/fortune",
                    httpEntity,
                    FortuneResponseDTO.class
            );

            if (response.getStatusCode().is2xxSuccessful() && Objects.requireNonNull(response.getBody()).getData() != null) {
                return response.getBody().getData();
            } else {
                log.warn("Fortune service returned non-200 status: {}, body: {}",
                        response.getStatusCode(), response.getBody());
            }

        } catch (Exception e) {
            log.warn("Error occurred while calling fortune service: {}", e.getMessage());
        }

        return DevLuckDTO.builder().overall(fortuneServiceErrorMessage).build();
    }

}
