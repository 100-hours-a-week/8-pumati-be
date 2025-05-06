package com.tebutebu.apiserver.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Log4j2
@Component
public class CustomLoginFailHandler implements AuthenticationFailureHandler {

    @Value("${frontend.redirect-uri}")
    private String frontendRedirectUri;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        response.setContentType("application/json; charset=UTF-8");

        if (exception instanceof OAuth2AuthenticationException oauthEx) {
            OAuth2Error err = oauthEx.getError();
            if ("additionalInfoRequired".equals(err.getErrorCode())) {
                String signupToken = err.getDescription();

                String redirectUrl = frontendRedirectUri +
                        "?message=additionalInfoRequired" +
                        "&signupToken=" + URLEncoder.encode(signupToken, StandardCharsets.UTF_8);

                response.sendRedirect(redirectUrl);
                return;
            }
        }

        String redirectUrl = frontendRedirectUri +
                "?message=loginFailed" +
                "&error=" + URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8);

        response.sendRedirect(redirectUrl);

        log.warn("Authentication failed for request [{}]: {}", request.getRequestURI(), exception.getMessage());
    }

}
