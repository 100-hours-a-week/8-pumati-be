package com.tebutebu.apiserver.service.mail.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tebutebu.apiserver.dto.mail.request.MailSendRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class DlqConsumer {

    private final ObjectMapper objectMapper;

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

            log.warn("Message permanently failed after retries. Consider manual review. to={}, subject={}",
                    dto.getEmail(), dto.getSubject());

        } catch (Exception e) {
            log.error("Failed to parse DLQ message: {}", record.value(), e);
        }
    }

    private boolean isValid(MailSendRequestDTO dto) {
        return dto.getEmail() != null && !dto.getEmail().isBlank()
                && dto.getSubject() != null && !dto.getSubject().isBlank()
                && dto.getContent() != null && !dto.getContent().isBlank();
    }

}
