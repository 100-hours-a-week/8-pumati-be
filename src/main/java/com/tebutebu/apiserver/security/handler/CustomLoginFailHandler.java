package com.tebutebu.apiserver.security.handler;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.Map;

@Log4j2
public class CustomLoginFailHandler implements AuthenticationFailureHandler {

    private final Gson gson = new Gson();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        response.setContentType("application/json; charset=UTF-8");

        if (exception instanceof OAuth2AuthenticationException oauthEx) {
            OAuth2Error err = oauthEx.getError();
            if ("additionalInfoRequired".equals(err.getErrorCode())) {
                // 200 OK + signupToken 반환
                response.setStatus(HttpServletResponse.SC_OK);
                String signupToken = err.getDescription();
                Map<String, Object> body = Map.of(
                        "message", "additionalInfoRequired",
                        "data", Map.of("signupToken", signupToken)
                );
                try (PrintWriter out = response.getWriter()) {
                    out.write(gson.toJson(body));
                }
                return;
            }
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        Map<String, Object> dataMap = Map.of(
                "timestamp", Instant.now().toString(),
                "status",    HttpServletResponse.SC_UNAUTHORIZED,
                "error",     "Unauthorized",
                "message",   "invalidCredentials",
                "path",      request.getRequestURI()
        );
        try (PrintWriter out = response.getWriter()) {
            out.write(gson.toJson(dataMap));
        }

        log.warn("Authentication failed for request [{}]: {}", request.getRequestURI(), exception.getMessage());
    }

}
