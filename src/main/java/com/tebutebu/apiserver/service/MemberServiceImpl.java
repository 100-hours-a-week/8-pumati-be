package com.tebutebu.apiserver.service;

import com.github.javafaker.Faker;
import com.tebutebu.apiserver.domain.Member;
import com.tebutebu.apiserver.domain.Team;
import com.tebutebu.apiserver.dto.member.request.MemberOAuthSignupRequestDTO;
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

@Service
@Log4j2
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    private final PasswordEncoder passwordEncoder;

    private final TeamService teamService;

    private final OAuthService oauthService;

    @Override
    public Long registerOAuthUser(MemberOAuthSignupRequestDTO dto) {

        Map<String,Object> claims = JWTUtil.validateToken(dto.getSignupToken());
        String provider = (String) claims.get("provider");
        String providerId = (String) claims.get("providerId");
        String email = (String) claims.get("email");

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
