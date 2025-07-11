package com.tebutebu.apiserver.service.ai.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tebutebu.apiserver.dto.ai.report.request.WeeklyReportImageRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Log4j2
@Service
@RequiredArgsConstructor
public class AiWeeklyReportImageRequestServiceImpl implements AiWeeklyReportImageRequestService {

    private final RestTemplate restTemplate = new RestTemplate();

    private final ObjectMapper mapper;

    @Value("${ai.report.service.url}")
    private String aiReportServiceUrl;

    @Override
    public String requestGenerateWeeklyReportImage(WeeklyReportImageRequestDTO request) {
        try {
            String json = mapper.writeValueAsString(request);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(json, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    aiReportServiceUrl + "/api/reports/image",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("AI Report image request success: {}", response.getBody());
                return response.getBody();
            } else {
                log.warn("AI️ Badge image request failed with non-2xx: {}, body: {}", response.getStatusCode(), response.getBody());
                return null;
            }

        } catch (Exception e) {
            log.error("Error calling AI report image service: {}", e.getMessage());
            return null;
        }
    }

}
