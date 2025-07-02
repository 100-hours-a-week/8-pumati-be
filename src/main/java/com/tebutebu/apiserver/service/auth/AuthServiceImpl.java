package com.tebutebu.apiserver.service.auth;

import com.tebutebu.apiserver.domain.Member;
import com.tebutebu.apiserver.dto.token.TokensDTO;
import com.tebutebu.apiserver.dto.token.request.RefreshTokenRotateRequestDTO;
import com.tebutebu.apiserver.dto.token.response.RefreshTokenResponseDTO;
import com.tebutebu.apiserver.repository.MemberRepository;
import com.tebutebu.apiserver.security.dto.CustomOAuth2User;
import com.tebutebu.apiserver.service.token.RefreshTokenService;
import com.tebutebu.apiserver.util.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final RefreshTokenService refreshTokenService;

    private final MemberRepository memberRepository;

    @Value("${spring.jwt.refresh-token.expiration}")
    private int refreshTokenExpiration;

    @Override
    public TokensDTO refreshTokens(String refreshTokenCookie) {
        if (refreshTokenCookie == null || refreshTokenCookie.isBlank()) {
            throw new IllegalArgumentException("nullRefreshToken");
        }

        RefreshTokenResponseDTO storedDto = refreshTokenService.findByToken(refreshTokenCookie);
        if (storedDto.isExpired()) {
            throw new IllegalArgumentException("expiredRefreshToken");
        }

        Map<String, Object> claims;
        try {
            claims = JWTUtil.validateToken(refreshTokenCookie);
        } catch (ExpiredJwtException e) {
            throw new IllegalArgumentException("expiredRefreshToken");
        } catch (JwtException e) {
            throw new IllegalArgumentException("invalidRefreshToken");
        }

        Long memberId = parseMemberId(claims.get("sub"));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("memberNotFound"));

        CustomOAuth2User customOAuth2User = new CustomOAuth2User(member);
        Map<String, Object> attributes = customOAuth2User.getAttributes();

        String newAccessToken = JWTUtil.generateAccessToken(attributes);

        RefreshTokenRotateRequestDTO rotateDto = RefreshTokenRotateRequestDTO.builder()
                .memberId(storedDto.getMemberId())
                .oldToken(storedDto.getToken())
                .newExpiryMinutes(refreshTokenExpiration)
                .build();
        RefreshTokenResponseDTO rotated = refreshTokenService.rotateToken(rotateDto);

        return TokensDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(rotated.getToken())
                .build();
    }

    private Long parseMemberId(Object subClaim) {
        try {
            return Long.parseLong(String.valueOf(subClaim));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("invalidSubClaim");
        }
    }

}
