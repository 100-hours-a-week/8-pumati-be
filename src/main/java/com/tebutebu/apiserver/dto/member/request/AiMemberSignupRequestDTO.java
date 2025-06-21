package com.tebutebu.apiserver.dto.member.request;

import com.tebutebu.apiserver.global.constant.ValidationMessages;
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
@NoArgsConstructor
@AllArgsConstructor
public class AiMemberSignupRequestDTO {

    @NotBlank(message = ValidationMessages.MEMBER_NAME_REQUIRED)
    @Size(max = 10, message = ValidationMessages.MEMBER_NAME_MAX_LENGTH_EXCEEDED)
    private String name;

    @NotBlank(message = ValidationMessages.MEMBER_NICKNAME_REQUIRED)
    @Size(max = 50, message = ValidationMessages.MEMBER_NICKNAME_MAX_LENGTH_EXCEEDED)
    private String nickname;

}
