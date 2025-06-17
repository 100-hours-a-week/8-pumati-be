package com.tebutebu.apiserver.service.ai.badge;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tebutebu.apiserver.dto.ai.badge.request.TeamBadgeImageUpdateRequestDTO;
import com.tebutebu.apiserver.dto.project.request.ProjectSummaryDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Log4j2
@RequiredArgsConstructor
public class AiBadgeImageRequestServiceImpl implements AiBadgeImageRequestService {

    private final RestTemplate restTemplate = new RestTemplate();

    private final ObjectMapper mapper;

    @Value("${ai.badge.service.url}")
    private String aiBadgeServiceUrl;

    @Override
    public void requestGenerateBadgeImage(ProjectSummaryDTO request) {
        sendBadgeImage(request, HttpMethod.POST);
    }

    @Override
    public boolean requestUpdateBadgeImage(TeamBadgeImageUpdateRequestDTO request) {
        return sendBadgeImage(request, HttpMethod.PUT);
    }

    private boolean sendBadgeImage(Object request, HttpMethod method) {
        try {
            String jsonBody = mapper.writeValueAsString(request);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> httpEntity = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<Void> response = restTemplate.exchange(
                    aiBadgeServiceUrl + "/api/badges/image",
                    method,
                    httpEntity,
                    Void.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("AI Badge image request success: {}", response.getBody());
                return true;
            } else {
                log.warn("AI️ Badge image request failed with non-2xx: {}, body: {}", response.getStatusCode(), response.getBody());
                return false;
            }

        } catch (Exception e) {
            log.warn("Error calling AI badge service: {}", e.getMessage());
            return false;
        }
    }

}
