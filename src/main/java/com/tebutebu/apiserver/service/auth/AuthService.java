package com.tebutebu.apiserver.service.auth;

import com.tebutebu.apiserver.dto.token.TokensDTO;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface AuthService {

    TokensDTO refreshTokens(String authorizationHeader, String refreshTokenCookie);

}
