package com.tebutebu.apiserver.service;

import com.tebutebu.apiserver.domain.RefreshToken;
import com.tebutebu.apiserver.dto.token.request.RefreshTokenCreateRequestDTO;
import com.tebutebu.apiserver.dto.token.request.RefreshTokenRotateRequestDTO;
import com.tebutebu.apiserver.dto.token.response.RefreshTokenResponseDTO;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface RefreshTokenService {

    void createOrUpdateRefreshToken(RefreshTokenCreateRequestDTO dto);

    void persistRefreshToken(Long memberId, String refreshToken);

    @Transactional(readOnly = true)
    RefreshTokenResponseDTO findByToken(String token);

    RefreshTokenResponseDTO rotateToken(RefreshTokenRotateRequestDTO dto);

    void deleteByMemberId(Long memberId);

    default RefreshTokenResponseDTO entityToDTO(RefreshToken refreshToken) {
        return RefreshTokenResponseDTO.builder()
                .id(refreshToken.getId())
                .memberId(refreshToken.getMember().getId())
                .token(refreshToken.getToken())
                .expiresAt(refreshToken.getExpiresAt())
                .build();
    }

}
