package com.tebutebu.apiserver.controller;

import com.tebutebu.apiserver.dto.token.TokensDTO;
import com.tebutebu.apiserver.service.auth.AuthService;
import com.tebutebu.apiserver.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

/**
 * OAuth2 인증 관련 컨트롤러
 * 카카오 로그인 콜백 처리를 담당합니다.
 */
@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/auth")
public class AuthController {

    @Value("${spring.jwt.refresh.cookie.name}")
    private String refreshCookieName;

    @Value("${jwt.refresh.cookie.max-age}")
    private int refreshCookieMaxAge;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id:NOT_SET}")
    private String kakaoClientId;
    
    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri:NOT_SET}")
    private String kakaoRedirectUri;
    
    @Value("${frontend.redirect-uri:NOT_SET}")
    private String frontendRedirectUri;

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

    /**
     * 카카오 OAuth2 로그인 시작 엔드포인트
     * 사용자를 카카오 로그인 페이지로 리다이렉트합니다.
     */
    @GetMapping("/kakao/login")
    public void kakaoLogin(HttpServletResponse response) throws IOException {
        log.info("카카오 로그인 요청 시작");
        // Spring Security OAuth2가 자동으로 처리하므로 /oauth2/authorization/kakao로 리다이렉트
        response.sendRedirect("/oauth2/authorization/kakao");
    }

    /**
     * 카카오 OAuth2 콜백 처리 엔드포인트 (리다이렉션용)
     * 이 엔드포인트는 Spring Security OAuth2LoginAuthenticationFilter가 처리합니다.
     */
    @GetMapping("/kakao/redirection")
    public void kakaoCallback(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.info("카카오 OAuth2 콜백 수신: {}", request.getQueryString());
        
        // 이 메서드는 실제로는 호출되지 않을 수 있습니다.
        // Spring Security의 OAuth2LoginAuthenticationFilter가 먼저 처리하기 때문입니다.
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("현재 인증 상태: {}", auth);
    }

    /**
     * 카카오 OAuth2 콜백 처리 엔드포인트 (실제 콜백)
     * 카카오에서 authorization code와 함께 리다이렉트되는 엔드포인트입니다.
     */
    @GetMapping("/kakao")
    public void kakaoOAuth2Callback(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "error", required = false) String error,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        
        log.info("카카오 OAuth2 콜백 처리 시작");
        log.info("Authorization Code: {}", code);
        log.info("State: {}", state);
        log.info("Error: {}", error);
        log.info("Request URI: {}", request.getRequestURI());
        log.info("Query String: {}", request.getQueryString());
        
        if (error != null) {
            log.error("카카오 OAuth2 에러 발생: {}", error);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "OAuth2 인증 실패: " + error);
            return;
        }
        
        // 이 지점에서 Spring Security의 OAuth2LoginAuthenticationFilter가 
        // 자동으로 처리해야 하지만, 만약 여기까지 왔다면 필터가 제대로 작동하지 않는 것입니다.
        log.warn("OAuth2LoginAuthenticationFilter가 콜백을 처리하지 않음. 수동 처리 필요");
        
        // 인증 상태 확인
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("현재 인증 상태: {}", auth);
        
        if (auth != null && auth.isAuthenticated()) {
            log.info("인증 성공, 사용자: {}", auth.getName());
        } else {
            log.warn("인증되지 않은 상태");
        }
    }

    /**
     * OAuth2 설정 상태를 확인하는 디버깅 엔드포인트
     * 쿠버네티스 환경에서 환경변수가 제대로 로드되었는지 확인합니다.
     */
    @GetMapping("/debug/config")
    public Map<String, Object> debugConfig() {
        log.info("OAuth2 설정 디버그 요청");
        
        return Map.of(
            "kakaoClientId", kakaoClientId.substring(0, Math.min(8, kakaoClientId.length())) + "...", // 보안을 위해 일부만 표시
            "kakaoRedirectUri", kakaoRedirectUri,
            "frontendRedirectUri", frontendRedirectUri,
            "activeProfile", System.getProperty("spring.profiles.active", "NONE"),
            "serverPort", System.getProperty("server.port", "8080")
        );
    }
}
