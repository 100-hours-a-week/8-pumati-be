package com.tebutebu.apiserver.service;

import com.tebutebu.apiserver.domain.Member;
import com.tebutebu.apiserver.dto.member.request.MemberOAuthSignupRequestDTO;
import com.tebutebu.apiserver.dto.member.response.MemberResponseDTO;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface MemberService {

    MemberResponseDTO get(Long memberId);

    Long registerOAuthUser(MemberOAuthSignupRequestDTO dto);

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
