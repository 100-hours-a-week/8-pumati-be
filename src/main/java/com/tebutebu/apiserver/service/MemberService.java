package com.tebutebu.apiserver.service;

import com.tebutebu.apiserver.domain.Member;
import com.tebutebu.apiserver.dto.member.request.MemberOAuthSignupRequestDTO;
import com.tebutebu.apiserver.dto.member.request.MemberUpdateRequestDTO;
import com.tebutebu.apiserver.dto.member.response.MemberResponseDTO;
import com.tebutebu.apiserver.dto.member.response.MemberSignupResponseDTO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface MemberService {

    @Transactional(readOnly = true)
    MemberResponseDTO get(Long memberId);

    @Transactional(readOnly = true)
    MemberResponseDTO get(String authorizationHeader);

    MemberSignupResponseDTO registerOAuthUser(MemberOAuthSignupRequestDTO dto, HttpServletResponse response);

    void modify(String authorizationHeader, MemberUpdateRequestDTO dto);

    void delete(String authorizationHeader);

    Member dtoToEntity(MemberOAuthSignupRequestDTO dto, String email);

    default MemberResponseDTO entityToDTO(Member member) {
        return MemberResponseDTO.builder()
                .id(member.getId())
                .teamId(member.getTeam() != null ? member.getTeam().getId() : null)
                .email(member.getEmail())
                .isSocial(member.isSocial())
                .name(member.getName())
                .nickname(member.getNickname())
                .course(member.getCourse())
                .profileImageUrl(member.getProfileImageUrl())
                .role(member.getRole())
                .state(member.getState())
                .createdAt(member.getCreatedAt())
                .modifiedAt(member.getModifiedAt())
                .build();
    }

}
