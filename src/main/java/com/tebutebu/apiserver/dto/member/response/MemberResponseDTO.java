package com.tebutebu.apiserver.dto.member.response;

import com.tebutebu.apiserver.domain.Course;
import com.tebutebu.apiserver.domain.MemberRole;
import com.tebutebu.apiserver.domain.MemberState;
import lombok.Getter;
import lombok.Builder;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class MemberResponseDTO {

    private Long id;

    private Long teamId;

    private Integer term;

    private Integer teamNumber;

    private String email;

    private String password;

    private boolean isSocial;

    private String name;

    private String nickname;

    private Course course;

    private String profileImageUrl;

    private MemberRole role;

    private MemberState state;

    private LocalDateTime createdAt, modifiedAt;

}
