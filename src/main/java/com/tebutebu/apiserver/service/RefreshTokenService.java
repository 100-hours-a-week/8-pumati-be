package com.tebutebu.apiserver.service;

import com.tebutebu.apiserver.domain.RefreshToken;
import com.tebutebu.apiserver.dto.refreshtoken.request.RefreshTokenCreateRequestDTO;
import com.tebutebu.apiserver.dto.refreshtoken.request.RefreshTokenRotateRequestDTO;
import com.tebutebu.apiserver.dto.refreshtoken.response.RefreshTokenResponseDTO;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface RefreshTokenService {

    RefreshTokenResponseDTO createOrUpdateRefreshToken(RefreshTokenCreateRequestDTO dto);

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
