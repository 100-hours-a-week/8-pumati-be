package com.tebutebu.apiserver.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

public class CookieUtil {

    private CookieUtil() {
    }

    public static Cookie createHttpOnlyCookie(String name, String value, int maxAgeSec) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeSec);
        return cookie;
    }

    public static Cookie createSecureHttpOnlyCookie(String name, String value, int maxAgeSec, boolean isSecure) {
        Cookie cookie = createHttpOnlyCookie(name, value, maxAgeSec);
        cookie.setSecure(isSecure);
        return cookie;
    }

    public static void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

}
