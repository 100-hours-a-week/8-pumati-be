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

@Service
@Log4j2
@RequiredArgsConstructor
public class FortuneServiceImpl implements FortuneService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${fortune.service.url}")
    private String fortuneServiceUrl;

    @Override
    public DevLuckDTO getnerateDevLuck(FortuneGenerateRequestDTO request) {
        HttpEntity<FortuneGenerateRequestDTO> httpEntity = new HttpEntity<>(request);
        ResponseEntity<FortuneResponseDTO> response = restTemplate.postForEntity(
                fortuneServiceUrl + "/api/llm/fortune",
                httpEntity,
                FortuneResponseDTO.class
        );

        return (response.getBody() != null && response.getBody().getData() != null)
                ? response.getBody().getData()
                : DevLuckDTO.builder().build();
    }

}
