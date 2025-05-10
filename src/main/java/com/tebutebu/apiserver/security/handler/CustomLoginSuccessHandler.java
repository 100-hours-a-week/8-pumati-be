package com.tebutebu.apiserver.security.handler;

import com.tebutebu.apiserver.security.dto.CustomOAuth2User;
import com.tebutebu.apiserver.service.RefreshTokenService;
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

    @Value("${spring.jwt.access-token.expiration}")
    private int accessTokenExpiration;

    @Value("${spring.jwt.refresh-token.expiration}")
    private int refreshTokenExpiration;

    @Value("${spring.jwt.refresh.cookie.name}")
    private String refreshCookieName;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        Map<String, Object> originalAttributes = customOAuth2User.getAttributes();
        Map<String, Object> responseData = new HashMap<>(originalAttributes);

        String accessToken = JWTUtil.generateToken(originalAttributes, accessTokenExpiration);
        String refreshToken = JWTUtil.generateToken(originalAttributes, refreshTokenExpiration);

        refreshTokenService.persistRefreshToken(customOAuth2User.getMemberId(), refreshToken);

        responseData.put("accessToken", accessToken);

        int maxAge = 60 * 60 * 24;
        if (request.isSecure()) {
            CookieUtil.addSecureRefreshTokenCookie(
                    response,
                    refreshCookieName,
                    refreshToken,
                    maxAge
            );
        } else {
            CookieUtil.addRefreshTokenCookie(
                    response,
                    refreshCookieName,
                    refreshToken,
                    maxAge
            );
        }

        String redirectUrl = frontendRedirectUri +
                "?message=loginSuccess" +
                "&accessToken=" + accessToken;
        response.sendRedirect(redirectUrl);
    }

}
