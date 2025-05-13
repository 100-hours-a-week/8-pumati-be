package com.tebutebu.apiserver.util;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CookieUtil {

    @Value("${frontend.is-local:false}")
    private boolean isFrontendLocal;

    public void addRefreshTokenCookie(
            HttpServletResponse response,
            String name,
            String value,
            int maxAgeSec,
            boolean secure
    ) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite("None")
                .path("/")
                .maxAge(maxAgeSec);

        if (isFrontendLocal) {
            builder.domain("localhost");
        }

        ResponseCookie cookie = builder.build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void deleteRefreshTokenCookie(
            HttpServletResponse response,
            String name,
            boolean secure
    ) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite("None")
                .path("/")
                .maxAge(0);

        if (isFrontendLocal) {
            builder.domain("localhost");
        }

        ResponseCookie cookie = builder.build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

}
