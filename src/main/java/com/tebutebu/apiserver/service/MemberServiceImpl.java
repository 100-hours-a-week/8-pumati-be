package com.tebutebu.apiserver.service;

import com.github.javafaker.Faker;
import com.tebutebu.apiserver.domain.Member;
import com.tebutebu.apiserver.domain.MemberRole;
import com.tebutebu.apiserver.domain.MemberState;
import com.tebutebu.apiserver.domain.Team;
import com.tebutebu.apiserver.dto.member.request.AiMemberSignupRequestDTO;
import com.tebutebu.apiserver.dto.member.request.MemberOAuthSignupRequestDTO;
import com.tebutebu.apiserver.dto.member.request.MemberUpdateRequestDTO;
import com.tebutebu.apiserver.dto.member.response.MemberResponseDTO;
import com.tebutebu.apiserver.dto.member.response.MemberSignupResponseDTO;
import com.tebutebu.apiserver.dto.oauth.request.OAuthCreateRequestDTO;
import com.tebutebu.apiserver.dto.team.response.TeamResponseDTO;
import com.tebutebu.apiserver.repository.MemberRepository;
import com.tebutebu.apiserver.security.dto.CustomOAuth2User;
import com.tebutebu.apiserver.util.CookieUtil;
import com.tebutebu.apiserver.util.JWTUtil;
import com.tebutebu.apiserver.util.exception.CustomValidationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    @Value("${spring.jwt.refresh.cookie.name}")
    private String refreshCookieName;

    @Value("${default.profile.image.url}")
    private String defaultProfileImageUrl;

    private final MemberRepository memberRepository;

    private final PasswordEncoder passwordEncoder;

    private final TeamService teamService;

    private final OAuthService oauthService;

    private final RefreshTokenService refreshTokenService;

    private final CookieUtil cookieUtil;

    @Override
    public MemberResponseDTO get(Long memberId) {
        Member member = memberRepository.findByIdWithTeam(memberId)
                .orElseThrow(() -> new NoSuchElementException("memberNotFound"));
        return entityToDTO(member);
    }

    @Override
    public MemberResponseDTO get(String authorizationHeader) {
        Long memberId = extractMemberIdFromHeader(authorizationHeader);
        Member member = memberRepository.findByIdWithTeam(memberId)
                .orElseThrow(() -> new NoSuchElementException("memberNotFound"));
        return entityToDTO(member);
    }

    @Override
    public List<MemberResponseDTO> getMembersByTeamId(Long teamId) {
        List<Member> members = memberRepository.findAllByTeamId(teamId);
        if (members.isEmpty()) {
            throw new NoSuchElementException("membersNotFound");
        }
        return members.stream()
                .map(this::entityToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public MemberSignupResponseDTO registerOAuthUser(MemberOAuthSignupRequestDTO dto,
                                                     HttpServletRequest request,
                                                     HttpServletResponse response) {

        var auth = JWTUtil.parseSignupToken(dto.getSignupToken());
        String provider = auth.provider();
        String providerId = auth.providerId();
        String email = auth.email();

        if (memberRepository.existsByEmail(email)) {
            throw new CustomValidationException("emailAlreadyExists");
        }

        Member member = memberRepository.save(dtoToEntity(dto, email));

        CustomOAuth2User customOAuth2User = new CustomOAuth2User(member);
        Map<String, Object> attributes = customOAuth2User.getAttributes();

        Long memberId = member.getId();

        String accessToken = JWTUtil.generateAccessToken(attributes);
        String refreshToken= JWTUtil.generateRefreshToken(memberId);
        refreshTokenService.persistRefreshToken(memberId, refreshToken);

        int maxAge = 60 * 60 * 24;
        cookieUtil.addRefreshTokenCookie(
                response,
                refreshCookieName,
                refreshToken,
                maxAge,
                request.isSecure()
        );

        OAuthCreateRequestDTO oauthDto = OAuthCreateRequestDTO.builder()
                .memberId(member.getId())
                .provider(provider)
                .providerId(providerId)
                .build();
        oauthService.register(oauthDto);

        return MemberSignupResponseDTO.builder()
                .id(member.getId())
                .teamId(member.getTeam() != null ? member.getTeam().getId() : null)
                .email(member.getEmail())
                .name(member.getName())
                .nickname(member.getNickname())
                .role(member.getRole().name())
                .state(member.getState().name())
                .accessToken(accessToken)
                .build();
    }

    @Override
    public Long registerAiMember(AiMemberSignupRequestDTO dto) {
        String email = generateUniqueAiEmail();

        Member member = Member.builder()
                .email(email)
                .password(passwordEncoder.encode("!A1ai-comment"))
                .name(dto.getName())
                .nickname(dto.getNickname())
                .profileImageUrl(defaultProfileImageUrl)
                .isSocial(false)
                .role(MemberRole.USER)
                .state(MemberState.ACTIVE)
                .build();
        return memberRepository.save(member).getId();
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

    @Override
    public void delete(String authorizationHeader, HttpServletRequest request, HttpServletResponse response) {
        Long memberId = extractMemberIdFromHeader(authorizationHeader);

        refreshTokenService.deleteByMemberId(memberId);
        oauthService.deleteByMemberId(memberId);

        if (!memberRepository.existsById(memberId)) {
            throw new CustomValidationException("memberNotFound");
        }

        refreshTokenService.deleteByMemberId(memberId);
        cookieUtil.deleteRefreshTokenCookie(
                response,
                refreshCookieName,
                request.isSecure()
        );

        memberRepository.deleteById(memberId);
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
                .profileImageUrl(dto.getProfileImageUrl() == null ? defaultProfileImageUrl : dto.getProfileImageUrl())
                .role(dto.getRole())
                .build();
    }

    private String generateUniqueAiEmail() {
        return "ai_" + UUID.randomUUID() + "@tebutebu.ai";
    }

}
