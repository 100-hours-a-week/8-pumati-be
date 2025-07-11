package com.tebutebu.apiserver.service.mail;

public interface MailService {

    void sendMail(String to, String subject, String content);

}
