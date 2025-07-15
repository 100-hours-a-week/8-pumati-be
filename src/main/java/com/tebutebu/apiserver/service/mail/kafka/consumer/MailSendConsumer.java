package com.tebutebu.apiserver.service.mail.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tebutebu.apiserver.dto.mail.request.MailSendRequestDTO;
import com.tebutebu.apiserver.global.errorcode.BusinessErrorCode;
import com.tebutebu.apiserver.global.exception.BusinessException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class MailSendConsumer {

    private final ObjectMapper objectMapper;

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @KafkaListener(
            topics = "${spring.kafka.topic.mail-send}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(ConsumerRecord<String, String> record) {
        String rawMessage = record.value();
        log.info("Received mail request from Kafka topic='{}': {}", record.topic(), rawMessage);

        try {
            MailSendRequestDTO dto = objectMapper.readValue(rawMessage, MailSendRequestDTO.class);
            sendMail(dto);
            log.info("Mail successfully sent. to={}, subject={}", dto.getEmail(), dto.getSubject());
        } catch (Exception e) {
            log.error("Failed to process Kafka mail message: {}", e.getMessage(), e);
            throw new BusinessException(BusinessErrorCode.MAIL_SEND_PROCESSING_FAILED, e);
        }
    }

    private void sendMail(MailSendRequestDTO dto) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

        helper.setFrom(from);
        helper.setTo(dto.getEmail());
        helper.setSubject(dto.getSubject());
        helper.setText(dto.getContent(), true);

        mailSender.send(message);
    }

}
