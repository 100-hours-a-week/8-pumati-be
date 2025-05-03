package com.tebutebu.apiserver.util;

import jakarta.servlet.http.Cookie;

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

}
