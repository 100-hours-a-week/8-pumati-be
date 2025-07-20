package com.tebutebu.apiserver.service.mail.kafka.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tebutebu.apiserver.dto.mail.request.MailSendRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Log4j2
@Component
@RequiredArgsConstructor
public class MailSendProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper;

    @Value("${spring.kafka.topic.mail-send}")
    private String mailSendTopic;

    public void sendMail(MailSendRequestDTO dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);

            RecordHeaders headers = new RecordHeaders();
            headers.add(new RecordHeader("x-retry-count", "0".getBytes(StandardCharsets.UTF_8)));
            headers.add(new RecordHeader("x-retry-stage", "initial".getBytes(StandardCharsets.UTF_8)));
            headers.add(new RecordHeader("x-origin-topic", mailSendTopic.getBytes(StandardCharsets.UTF_8)));

            ProducerRecord<String, String> record = new ProducerRecord<>(
                    mailSendTopic,
                    null,
                    null,
                    null,
                    json,
                    headers
            );

            kafkaTemplate.send(record);
            log.info("Mail send request sent to Kafka. to={}, subject={}", dto.getEmail(), dto.getSubject());
        } catch (Exception e) {
            log.error("Failed to send mail message to Kafka: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send mail message to Kafka", e);
        }
    }

}
