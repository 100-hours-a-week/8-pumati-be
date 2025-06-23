package com.tebutebu.apiserver.service.auth;

import com.tebutebu.apiserver.domain.Member;
import com.tebutebu.apiserver.dto.token.TokensDTO;
import com.tebutebu.apiserver.dto.token.request.RefreshTokenRotateRequestDTO;
import com.tebutebu.apiserver.dto.token.response.RefreshTokenResponseDTO;
import com.tebutebu.apiserver.global.errorcode.BusinessErrorCode;
import com.tebutebu.apiserver.global.exception.BusinessException;
import com.tebutebu.apiserver.repository.MemberRepository;
import com.tebutebu.apiserver.security.dto.CustomOAuth2User;
import com.tebutebu.apiserver.service.token.RefreshTokenService;
import com.tebutebu.apiserver.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String BEARER_PREFIX = "Bearer ";

    private final RefreshTokenService refreshTokenService;

    private final MemberRepository memberRepository;

    @Value("${spring.jwt.refresh-token.expiration}")
    private int refreshTokenExpiration;

    @Override
    public TokensDTO refreshTokens(String authorizationHeader, String refreshTokenCookie) {
        if (refreshTokenCookie == null || refreshTokenCookie.isBlank()) {
            throw new BusinessException(BusinessErrorCode.INVALID_REFRESH_TOKEN);
        }

        boolean isValidAccessToken = false;
        String currentAccessToken = null;
        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
            currentAccessToken = authorizationHeader.substring(BEARER_PREFIX.length());
            isValidAccessToken = !isExpired(currentAccessToken);
        }

        if (isValidAccessToken) {
            return TokensDTO.builder()
                    .accessToken(currentAccessToken)
                    .refreshToken(refreshTokenCookie)
                    .build();
        }

        RefreshTokenResponseDTO storedDto = refreshTokenService.findByToken(refreshTokenCookie);
        if (storedDto.isExpired()) {
            throw new BusinessException(BusinessErrorCode.EXPIRED_REFRESH_TOKEN);
        }

        Map<String, Object> claims = JWTUtil.validateToken(refreshTokenCookie);
        Long memberId = ((Number) claims.get("sub")).longValue();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.MEMBER_NOT_FOUND));

        CustomOAuth2User customOAuth2User = new CustomOAuth2User(member);
        Map<String, Object> attributes = customOAuth2User.getAttributes();

        String newAccess = JWTUtil.generateAccessToken(attributes);

        RefreshTokenRotateRequestDTO rotateDto = RefreshTokenRotateRequestDTO.builder()
                .memberId(storedDto.getMemberId())
                .oldToken(storedDto.getToken())
                .newExpiryMinutes(refreshTokenExpiration)
                .build();
        RefreshTokenResponseDTO rotated = refreshTokenService.rotateToken(rotateDto);

        return TokensDTO.builder()
                .accessToken(newAccess)
                .refreshToken(rotated.getToken())
                .build();
    }

    private boolean isExpired(String token) {
        try {
            JWTUtil.validateToken(token);
            return false;
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            return msg != null && msg.toLowerCase().contains("expired");
        }
    }

}
