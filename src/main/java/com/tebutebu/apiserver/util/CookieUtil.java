package com.tebutebu.apiserver.util;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

public class CookieUtil {

    private CookieUtil() {}

    public static void addRefreshTokenCookie(
            HttpServletResponse response,
            String name,
            String value,
            int maxAgeSec
    ) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(false)
                .sameSite("None")
                .path("/")
                .maxAge(maxAgeSec)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public static void addSecureRefreshTokenCookie(
            HttpServletResponse response,
            String name,
            String value,
            int maxAgeSec
    ) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(maxAgeSec)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public static void deleteRefreshTokenCookie(
            HttpServletResponse response,
            String name
    ) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(false)
                .sameSite("None")
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public static void deleteSecureRefreshTokenCookie(
            HttpServletResponse response,
            String name
    ) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

}
