package com.tebutebu.apiserver.security.handler;

import com.google.gson.Gson;
import com.tebutebu.apiserver.security.dto.CustomOAuth2User;
import com.tebutebu.apiserver.util.JWTUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@AllArgsConstructor
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    private int accessTokenExpiration, refreshTokenExpiration;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        Map<String, Object> originalAttributes = customOAuth2User.getAttributes();
        Map<String, Object> responseData = new HashMap<>(originalAttributes);

        String accessToken = JWTUtil.generateToken(originalAttributes, accessTokenExpiration);
        String refreshToken = JWTUtil.generateToken(originalAttributes, refreshTokenExpiration);

        responseData.put("accessToken", accessToken);

        Cookie refreshCookie = createCookie("refreshToken", refreshToken);
        response.addCookie(refreshCookie);

        Map<String, Object> responseBody = Map.of(
                "message", "loginSuccess",
                "data", responseData
        );

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json; charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println(new Gson().toJson(responseBody));
        }
    }

    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(60 * 60 * 60);
        // cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        return cookie;
    }

}
