package com.tebutebu.apiserver.dto.member.response;

import lombok.Getter;
import lombok.Builder;
import lombok.AllArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
public class MemberSignupResponseDTO {

    private Long id;

    private Long teamId;

    private String email;

    private String name;

    private String nickname;

    private String role;

    private String state;

    private String accessToken;

}
