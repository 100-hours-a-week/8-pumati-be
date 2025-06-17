package com.tebutebu.apiserver.service.member;

import com.tebutebu.apiserver.domain.Member;
import com.tebutebu.apiserver.domain.Team;
import com.tebutebu.apiserver.dto.member.request.AiMemberSignupRequestDTO;
import com.tebutebu.apiserver.dto.member.request.MemberOAuthSignupRequestDTO;
import com.tebutebu.apiserver.dto.member.request.MemberUpdateRequestDTO;
import com.tebutebu.apiserver.dto.member.response.MemberResponseDTO;
import com.tebutebu.apiserver.dto.member.response.MemberSignupResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface MemberService {

    @Transactional(readOnly = true)
    MemberResponseDTO get(Long memberId);

    @Transactional(readOnly = true)
    MemberResponseDTO get(String authorizationHeader);

    @Transactional(readOnly = true)
    List<MemberResponseDTO> getMembersByTeamId(Long teamId);

    MemberSignupResponseDTO registerOAuthUser(MemberOAuthSignupRequestDTO dto, HttpServletRequest request, HttpServletResponse response);

    Long registerAiMember(AiMemberSignupRequestDTO dto);

    void modify(String authorizationHeader, MemberUpdateRequestDTO dto);

    void delete(String authorizationHeader, HttpServletRequest request, HttpServletResponse response);

    Member dtoToEntity(MemberOAuthSignupRequestDTO dto, String email);

    default MemberResponseDTO entityToDTO(Member member) {
        Team team = member.getTeam();
        return MemberResponseDTO.builder()
                .id(member.getId())
                .teamId(team != null ? team.getId() : null)
                .term(team != null ? team.getTerm() : null)
                .teamNumber(team != null ? team.getNumber() : null)
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
