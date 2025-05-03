package com.tebutebu.apiserver.security.handler;

import com.tebutebu.apiserver.service.RefreshTokenService;
import com.tebutebu.apiserver.util.CookieUtil;
import com.tebutebu.apiserver.util.JWTUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {

    @Value("${spring.jwt.refresh.cookie.name}")
    private String refreshCookieName;

    private final RefreshTokenService refreshTokenService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Optional<Cookie> cookie = Arrays.stream(Optional.ofNullable(request.getCookies()).orElse(new Cookie[0]))
                .filter(c -> refreshCookieName.equals(c.getName()))
                .findFirst();

        cookie.ifPresent(c -> {
            String refreshToken = c.getValue();
            Map<String,Object> claims = JWTUtil.validateToken(refreshToken);
            Long memberId = ((Number) claims.get("id")).longValue();
            refreshTokenService.deleteByMemberId(memberId);
            CookieUtil.deleteCookie(response, refreshCookieName);
        });
    }
}
