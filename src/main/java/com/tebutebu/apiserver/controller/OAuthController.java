package com.tebutebu.apiserver.controller;

import com.tebutebu.apiserver.service.OAuthService;
import com.tebutebu.apiserver.util.exception.CustomValidationException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/oauth")
public class OAuthController {

    private final OAuthService oAuthService;

    @GetMapping("/{provider}/redirection")
    public void redirectToProvider(@PathVariable String provider, HttpServletResponse response) throws IOException {
        try {
            oAuthService.validateProvider(provider);
            response.sendRedirect("/oauth2/authorization/" + provider);
        } catch (CustomValidationException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

}
