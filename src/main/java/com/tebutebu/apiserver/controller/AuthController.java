package com.tebutebu.apiserver.controller;

import com.tebutebu.apiserver.dto.token.TokensDTO;
import com.tebutebu.apiserver.service.AuthService;
import com.tebutebu.apiserver.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/auth")
public class AuthController {

    @Value("${spring.jwt.refresh.cookie.name}")
    private String refreshCookieName;

    @Value("${jwt.refresh.cookie.max-age}")
    private int refreshCookieMaxAge;

    private final AuthService authService;

    private final CookieUtil cookieUtil;

    @PutMapping("/tokens")
    public ResponseEntity<?> refreshToken(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @CookieValue(value = "${spring.jwt.refresh.cookie.name}", required = false) String refreshTokenCookie,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        try {
            TokensDTO tokens = authService.refreshTokens(authorizationHeader, refreshTokenCookie);

            cookieUtil.addRefreshTokenCookie(
                    response,
                    refreshCookieName,
                    tokens.getRefreshToken(),
                    refreshCookieMaxAge,
                    request.isSecure()
            );

            Map<String, Object> body = Map.of(
                    "message", "refreshSuccess",
                    "data", Map.of("accessToken", tokens.getAccessToken())
            );
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            cookieUtil.deleteRefreshTokenCookie(response, refreshCookieName, request.isSecure());
            throw e;
        }
    }

}
