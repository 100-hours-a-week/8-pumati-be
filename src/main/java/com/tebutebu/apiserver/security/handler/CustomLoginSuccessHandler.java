package com.tebutebu.apiserver.security.handler;

import com.tebutebu.apiserver.security.dto.CustomOAuth2User;
import com.tebutebu.apiserver.service.token.RefreshTokenService;
import com.tebutebu.apiserver.util.CookieUtil;
import com.tebutebu.apiserver.util.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@Component
@RequiredArgsConstructor
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final RefreshTokenService refreshTokenService;

    @Value("${frontend.redirect-uri}")
    private String frontendRedirectUri;

    @Value("${spring.jwt.refresh.cookie.name}")
    private String refreshCookieName;

    @Value("${jwt.refresh.cookie.max-age}")
    private int refreshCookieMaxAge;

    private final CookieUtil cookieUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        Map<String, Object> originalAttributes = customOAuth2User.getAttributes();
        Map<String, Object> responseData = new HashMap<>(originalAttributes);

        Long memberId = customOAuth2User.getMemberId();

        String accessToken = JWTUtil.generateAccessToken(originalAttributes);
        String refreshToken = JWTUtil.generateRefreshToken(memberId);

        refreshTokenService.persistRefreshToken(memberId, refreshToken);

        responseData.put("accessToken", accessToken);

        cookieUtil.addRefreshTokenCookie(
                response,
                refreshCookieName,
                refreshToken,
                refreshCookieMaxAge,
                request.isSecure()
        );

        String redirectUrl = frontendRedirectUri +
                "?message=loginSuccess" +
                "&accessToken=" + accessToken;
        response.sendRedirect(redirectUrl);
    }

}
