package com.tebutebu.apiserver.controller;

import com.tebutebu.apiserver.dto.token.TokensDTO;
import com.tebutebu.apiserver.service.AuthService;
import com.tebutebu.apiserver.util.CookieUtil;
import jakarta.servlet.http.Cookie;
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
    private String REFRESH_COOKIE_NAME;

    private final AuthService authService;

    @PutMapping("/tokens")
    public ResponseEntity<?> refreshToken(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @CookieValue(value = "refreshToken", required = false) String refreshTokenCookie,
            HttpServletResponse response
    ) {
        TokensDTO tokens = authService.refreshTokens(authorizationHeader, refreshTokenCookie);

        Cookie refreshCookie = CookieUtil.createHttpOnlyCookie(
                REFRESH_COOKIE_NAME,
                tokens.getRefreshToken(),
                60 * 60 * 24
        );
        response.addCookie(refreshCookie);

        Map<String, Object> body = Map.of(
                "message", "refreshSuccess",
                "data", Map.of("accessToken", tokens.getAccessToken())
        );
        return ResponseEntity.ok(body);
    }

}
