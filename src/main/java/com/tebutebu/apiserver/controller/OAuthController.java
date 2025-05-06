package com.tebutebu.apiserver.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/oauth")
public class OAuthController {

    private static final List<String> ALLOWED = List.of("kakao");

    @GetMapping("/{provider}/redirection")
    public void redirectToProvider(@PathVariable String provider, HttpServletResponse response) throws IOException {
        if (!ALLOWED.contains(provider)) {
            response.sendError(HttpStatus.BAD_REQUEST.value(), "invalidProvider");
            return;
        }
        response.sendRedirect("/oauth2/authorization/" + provider);
    }

}
