package com.tebutebu.apiserver.controller;

import com.tebutebu.apiserver.service.oauth.OAuthService;
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
        oAuthService.validateProvider(provider);
        response.sendRedirect("/oauth2/authorization/" + provider);
    }

}
