package com.tebutebu.apiserver.dto.mail.request;

import com.tebutebu.apiserver.global.constant.ValidationMessages;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MailSendRequestDTO {

    @NotBlank(message = ValidationMessages.EMAIL_REQUIRED)
    @Email(message = ValidationMessages.EMAIL_INVALID)
    private String email;

    @NotBlank(message = ValidationMessages.MAIL_SUBJECT_REQUIRED)
    @Size(max = 100, message = ValidationMessages.MAIL_SUBJECT_MAX_LENGTH_EXCEEDED)
    private String subject;

    @NotBlank(message = ValidationMessages.MAIL_CONTENT_REQUIRED)
    @Size(max = 2000, message = ValidationMessages.MAIL_CONTENT_MAX_LENGTH_EXCEEDED)
    private String content;

}
