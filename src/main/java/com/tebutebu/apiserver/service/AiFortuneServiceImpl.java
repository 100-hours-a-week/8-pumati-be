package com.tebutebu.apiserver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tebutebu.apiserver.dto.fortune.request.AiFortuneGenerateRequestDTO;
import com.tebutebu.apiserver.dto.fortune.response.DevLuckDTO;
import com.tebutebu.apiserver.dto.fortune.response.FortuneResponseDTO;
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
public class AiFortuneServiceImpl implements AiFortuneService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${fortune.service.uri}")
    private String fortuneServiceUri;

    @Value("${fortune.service.error-message}")
    private String fortuneServiceErrorMessage;

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    public DevLuckDTO generateDevLuck(AiFortuneGenerateRequestDTO request) {
        try {
            String jsonBody = mapper.writeValueAsString(request);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> httpEntity = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<FortuneResponseDTO> response = restTemplate.exchange(
                    fortuneServiceUri,
                    HttpMethod.POST,
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
