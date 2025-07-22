package com.tebutebu.apiserver.service.mail.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tebutebu.apiserver.dto.mail.request.MailSendRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Log4j2
@Component
@RequiredArgsConstructor
public class DlqConsumer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper;

    @Value("${spring.kafka.topic.mail-send}")
    private String mailSendTopic;

    private static final int MAX_RETRY_COUNT = 3;

    @KafkaListener(
            topics = "${spring.kafka.topic.mail-send-dlq}",
            groupId = "${spring.kafka.consumer.group-id}-dlq"
    )
    public void listenDlq(ConsumerRecord<String, String> record) {
        log.error("Received message from DLQ - topic: {}, offset: {}, value: {}",
                record.topic(), record.offset(), record.value());

        try {
            MailSendRequestDTO dto = objectMapper.readValue(record.value(), MailSendRequestDTO.class);

            if (!isValid(dto)) {
                log.warn("Invalid DLQ message skipped: {}", record.value());
                return;
            }

            int retryCount = getRetryCount(record.headers());
            if (retryCount >= MAX_RETRY_COUNT) {
                log.warn("Retry limit exceeded. Skipping message: {}", record.value());
                return;
            }

            Headers headers = record.headers();
            headers.add(new RecordHeader("x-retry-count", String.valueOf(retryCount + 1).getBytes(StandardCharsets.UTF_8)));

            ProducerRecord<String, String> newRecord = new ProducerRecord<>(
                    mailSendTopic,
                    null,
                    record.key(),
                    record.value(),
                    headers
            );

            kafkaTemplate.send(newRecord);

            log.info("DLQ message resent to mail-send topic with retryCount={}", retryCount + 1);

        } catch (Exception e) {
            log.error("Failed to process DLQ message: {}", record.value(), e);
        }
    }

    private boolean isValid(MailSendRequestDTO dto) {
        return dto.getEmail() != null && !dto.getEmail().isBlank()
                && dto.getSubject() != null && !dto.getSubject().isBlank()
                && dto.getContent() != null && !dto.getContent().isBlank();
    }

    private int getRetryCount(Headers headers) {
        if (headers == null) {
            return 0;
        }

        return headers.lastHeader("x-retry-count") != null
                ? Integer.parseInt(new String(headers.lastHeader("x-retry-count").value(), StandardCharsets.UTF_8))
                : 0;
    }
}
