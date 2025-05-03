package com.tebutebu.apiserver.service;

import com.github.javafaker.Faker;
import com.tebutebu.apiserver.domain.Member;
import com.tebutebu.apiserver.domain.Team;
import com.tebutebu.apiserver.dto.member.request.MemberOAuthSignupRequestDTO;
import com.tebutebu.apiserver.dto.member.request.MemberUpdateRequestDTO;
import com.tebutebu.apiserver.dto.member.response.MemberResponseDTO;
import com.tebutebu.apiserver.dto.oauth.request.OAuthCreateRequestDTO;
import com.tebutebu.apiserver.dto.team.response.TeamResponseDTO;
import com.tebutebu.apiserver.repository.MemberRepository;
import com.tebutebu.apiserver.util.JWTUtil;
import com.tebutebu.apiserver.util.exception.CustomValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.NoSuchElementException;

@Service
@Log4j2
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    private final PasswordEncoder passwordEncoder;

    private final TeamService teamService;

    private final OAuthService oauthService;

    @Override
    public MemberResponseDTO get(Long memberId) {
        Member member = memberRepository.findByIdWithTeam(memberId)
                .orElseThrow(() -> new NoSuchElementException("userNotFound"));
        return entityToDTO(member);
    }

    @Override
    public Long registerOAuthUser(MemberOAuthSignupRequestDTO dto) {

        var auth = JWTUtil.parseSignupToken(dto.getSignupToken());
        String provider = auth.provider();
        String providerId = auth.providerId();
        String email = auth.email();

        if (memberRepository.existsByEmail(email)) {
            throw new CustomValidationException("emailAlreadyExists");
        }

        Member member = memberRepository.save(dtoToEntity(dto, email));

        OAuthCreateRequestDTO oauthDto = OAuthCreateRequestDTO.builder()
                .memberId(member.getId())
                .provider(provider)
                .providerId(providerId)
                .build();
        oauthService.register(oauthDto);
        return member.getId();
    }

    @Override
    public void modify(String authorizationHeader, MemberUpdateRequestDTO dto) {
        Long memberId = extractMemberIdFromHeader(authorizationHeader);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomValidationException("memberNotFound"));

        TeamResponseDTO teamDto = teamService.getByTermAndNumber(dto.getTerm(), dto.getTeamNumber());
        member.changeTeam(teamDto == null ? null : Team.builder().id(teamDto.getId()).build());

        if (dto.getProfileImageUrl() != null && !dto.getProfileImageUrl().isEmpty()) {
            member.changeProfileImageUrl(dto.getProfileImageUrl());
        }

        member.changeName(dto.getName());
        member.changeNickname(dto.getNickname());
        member.changeCourse(dto.getCourse());
        member.changeRole(dto.getRole());

        memberRepository.save(member);
    }

    public Long extractMemberIdFromHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("invalidToken");
        }
        String token = authorizationHeader.substring(7);
        Map<String,Object> claims = JWTUtil.validateToken(token);
        Number idClaim = (Number) claims.get("id");
        if (idClaim == null) {
            throw new IllegalArgumentException("invalidToken");
        }
        return idClaim.longValue();
    }

    @Override
    public Member dtoToEntity(MemberOAuthSignupRequestDTO dto, String email) {
        TeamResponseDTO teamResponseDTO = teamService.getByTermAndNumber(dto.getTerm(), dto.getTeamNumber());
        return Member.builder()
                .team(teamResponseDTO == null ? null : Team.builder().id(teamResponseDTO.getId()).build())
                .email(email)
                .password(passwordEncoder.encode(new Faker().internet().password()))
                .name(dto.getName())
                .nickname(dto.getNickname())
                .course(dto.getCourse())
                .profileImageUrl(dto.getProfileImageUrl())
                .role(dto.getRole())
                .build();
    }

}
