package com.tebutebu.apiserver.service.mail;

import com.tebutebu.apiserver.dto.mail.request.MailSendRequestDTO;
import com.tebutebu.apiserver.service.mail.kafka.producer.MailSendProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final MailSendProducer mailSendProducer;

    @Override
    public void sendMail(String to, String subject, String content) {
        MailSendRequestDTO dto = MailSendRequestDTO.builder()
                .email(to)
                .subject(subject)
                .content(content)
                .build();
        mailSendProducer.sendMail(dto);
    }

}
