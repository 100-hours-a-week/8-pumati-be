package com.tebutebu.apiserver.service.mail.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class DlqConsumer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.kafka.topic.mail-send}")
    private String mailSendTopic;

    @KafkaListener(
            topics = "${spring.kafka.topic.mail-send-dlq}",
            groupId = "${spring.kafka.consumer.group-id}-dlq"
    )
    public void listenDlq(ConsumerRecord<String, String> record) {
        log.error("Received message from DLQ - topic: {}, partition: {}, offset: {}, key: {}, value: {}",
                record.topic(), record.partition(), record.offset(), record.key(), record.value()
        );

        try {
            kafkaTemplate.send(mailSendTopic, record.key(), record.value());
            log.info("DLQ message successfully resent to mail-send topic.");
        } catch (Exception e) {
            log.error("Failed to resend message from DLQ to mail-send topic: {}", e.getMessage(), e);
        }
    }

}
