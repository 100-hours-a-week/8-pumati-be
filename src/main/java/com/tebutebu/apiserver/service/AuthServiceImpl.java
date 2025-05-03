package com.tebutebu.apiserver.service;

import com.tebutebu.apiserver.domain.RefreshToken;
import com.tebutebu.apiserver.dto.token.TokensDTO;
import com.tebutebu.apiserver.repository.RefreshTokenRepository;
import com.tebutebu.apiserver.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String BEARER_PREFIX = "Bearer ";

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${spring.jwt.access-token.expiration}")
    private int accessTokenExpiration;

    @Value("${spring.jwt.refresh-token.expiration}")
    private int refreshTokenExpiration;

    @Value("${spring.jwt.refresh.threshold}")
    private int refreshThreshold;

    @Override
    public TokensDTO refreshTokens(String authorizationHeader, String refreshTokenCookie) {
        validateInputs(authorizationHeader, refreshTokenCookie);

        String currentAccess = extractAccessToken(authorizationHeader);
        if (!isExpired(currentAccess)) {
            return TokensDTO.builder()
                    .accessToken(currentAccess)
                    .refreshToken(refreshTokenCookie)
                    .build();
        }

        Map<String, Object> claims = JWTUtil.validateToken(refreshTokenCookie);
        RefreshToken stored = loadAndVerifyStoredRefreshToken(refreshTokenCookie);

        String newAccess  = JWTUtil.generateToken(claims, accessTokenExpiration);
        String newRefresh = rotateRefreshToken(claims, stored);

        return TokensDTO.builder()
                .accessToken(newAccess)
                .refreshToken(newRefresh)
                .build();
    }

    private void validateInputs(String authHeader, String refreshCookie) {
        if (!StringUtils.hasText(refreshCookie)) {
            throw new IllegalArgumentException("nullRefreshToken");
        }
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            throw new IllegalArgumentException("invalidAccessTokenHeader");
        }
    }

    private String extractAccessToken(String authHeader) {
        return authHeader.substring(BEARER_PREFIX.length());
    }

    private RefreshToken loadAndVerifyStoredRefreshToken(String refreshToken) {
        RefreshToken stored = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("refreshTokenNotFound"));
        if (stored.isExpired()) {
            throw new IllegalArgumentException("expiredRefreshToken");
        }
        return stored;
    }

    private String rotateRefreshToken(Map<String,Object> claims, RefreshToken stored) {
        Instant expiresAt = Instant.ofEpochSecond(((Number)claims.get("exp")).longValue());
        long minutesLeft = Duration.between(Instant.now(), expiresAt).toMinutes();

        if (minutesLeft < refreshThreshold) {
            String rotated = JWTUtil.generateToken(claims, refreshTokenExpiration);
            stored.changeToken(rotated);
            stored.changeExpiresAt(LocalDateTime.now().plusMinutes(refreshTokenExpiration));
            refreshTokenRepository.save(stored);
            return rotated;
        }
        return stored.getToken();
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
