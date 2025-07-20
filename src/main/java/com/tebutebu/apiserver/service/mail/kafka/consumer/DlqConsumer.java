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

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

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

        MailSendRequestDTO dto;
        try {
            dto = objectMapper.readValue(record.value(), MailSendRequestDTO.class);
        } catch (Exception e) {
            log.warn("Malformed DLQ message skipped: {}", record.value());
            return;
        }

        if (!isValid(dto)) {
            log.warn("Invalid DLQ message skipped: {}", record.value());
            return;
        }

        Headers headers = record.headers();
        String retryStage = extractHeader(headers, "x-retry-stage");
        String errorCode = extractHeader(headers, "x-error-code");
        String errorMessage = extractHeader(headers, "x-error-message");
        int retryCount = parseRetryCount(headers);

        log.warn("DLQ message failure reason - errorCode='{}', errorMessage='{}', retryCount={}, retryStage={}",
                errorCode, errorMessage, retryCount, retryStage);

        if ("retry".equalsIgnoreCase(retryStage)) {
            log.warn("Message has already been retried once. Skipping further retry.");
            return;
        }

        if (retryCount >= MAX_RETRY_COUNT) {
            log.warn("DLQ message skipped - retry count exceeded: {}", retryCount);
            return;
        }

        if (shouldRetry(errorCode)) {
            int updatedRetryCount = retryCount + 1;
            updateHeader(headers, "x-retry-count", String.valueOf(updatedRetryCount));
            updateHeader(headers, "x-retry-stage", "retry");

            ProducerRecord<String, String> newRecord = new ProducerRecord<>(
                    mailSendTopic,
                    null,
                    null,
                    null,
                    record.value(),
                    headers
            );
            kafkaTemplate.send(newRecord);

            log.info("Retry triggered for DLQ message: to={}, subject={}, retryCount={}",
                    dto.getEmail(), dto.getSubject(), updatedRetryCount);
        } else {
            log.warn("Skipped DLQ message - retry not allowed for exception: {}", errorCode);
        }
    }

    private boolean isValid(MailSendRequestDTO dto) {
        return dto.getEmail() != null && !dto.getEmail().isBlank()
                && dto.getSubject() != null && !dto.getSubject().isBlank()
                && dto.getContent() != null && !dto.getContent().isBlank();
    }

    private String extractHeader(Headers headers, String key) {
        if (headers == null || headers.lastHeader(key) == null) return null;
        return new String(headers.lastHeader(key).value(), StandardCharsets.UTF_8);
    }

    private void updateHeader(Headers headers, String key, String value) {
        headers.remove(key);
        headers.add(new RecordHeader(key, value.getBytes(StandardCharsets.UTF_8)));
    }

    private int parseRetryCount(Headers headers) {
        try {
            String countStr = extractHeader(headers, "x-retry-count");
            return countStr != null ? Integer.parseInt(countStr) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private boolean shouldRetry(String errorCode) {
        return errorCode != null &&
                (errorCode.equals("MessagingException") || errorCode.equals("MailSendException"));
    }

}
