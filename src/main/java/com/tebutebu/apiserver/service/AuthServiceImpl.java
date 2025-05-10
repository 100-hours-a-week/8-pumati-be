package com.tebutebu.apiserver.service;

import com.tebutebu.apiserver.dto.token.TokensDTO;
import com.tebutebu.apiserver.dto.token.request.RefreshTokenRotateRequestDTO;
import com.tebutebu.apiserver.dto.token.response.RefreshTokenResponseDTO;
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

    @Value("${spring.jwt.access-token.expiration}")
    private int accessTokenExpiration;

    @Value("${spring.jwt.refresh-token.expiration}")
    private int refreshTokenExpiration;

    @Override
    public TokensDTO refreshTokens(String authorizationHeader, String refreshTokenCookie) {
        if (refreshTokenCookie == null || refreshTokenCookie.isBlank()) {
            throw new IllegalArgumentException("nullRefreshToken");
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
            throw new IllegalArgumentException("expiredRefreshToken");
        }

        Map<String, Object> claims = JWTUtil.validateToken(refreshTokenCookie);

        String newAccess = JWTUtil.generateToken(claims, accessTokenExpiration);

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
        } catch (RuntimeException ex) {
            return "Expired".equals(ex.getMessage());
        }
    }

}
