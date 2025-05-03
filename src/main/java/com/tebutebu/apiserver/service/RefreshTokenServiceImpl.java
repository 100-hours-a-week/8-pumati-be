package com.tebutebu.apiserver.service;

import com.tebutebu.apiserver.domain.Member;
import com.tebutebu.apiserver.domain.RefreshToken;
import com.tebutebu.apiserver.dto.token.request.RefreshTokenCreateRequestDTO;
import com.tebutebu.apiserver.dto.token.request.RefreshTokenRotateRequestDTO;
import com.tebutebu.apiserver.dto.token.response.RefreshTokenResponseDTO;
import com.tebutebu.apiserver.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Log4j2
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${spring.jwt.refresh-token.expiration}")
    private int refreshTokenExpiration;

    @Override
    public void createOrUpdateRefreshToken(RefreshTokenCreateRequestDTO dto) {
        refreshTokenRepository.findByMemberId(dto.getMemberId())
                .ifPresent(refreshTokenRepository::delete);

        RefreshToken saved = refreshTokenRepository.save(dtoToEntity(dto));
        entityToDTO(saved);
    }

    @Override
    public void persistRefreshToken(Long memberId, String refreshToken) {
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(refreshTokenExpiration);
        RefreshTokenCreateRequestDTO dto = RefreshTokenCreateRequestDTO.builder()
                .memberId(memberId)
                .token(refreshToken)
                .expiresAt(expiresAt)
                .build();
        createOrUpdateRefreshToken(dto);
    }

    @Override
    public RefreshTokenResponseDTO findByToken(String token) {
        RefreshToken entity = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new NoSuchElementException("refreshTokenNotFound"));
        return entityToDTO(entity);
    }

    @Override
    public RefreshTokenResponseDTO rotateToken(RefreshTokenRotateRequestDTO dto) {
        RefreshToken old = refreshTokenRepository.findByToken(dto.getOldToken())
                .orElseThrow(() -> new NoSuchElementException("refreshTokenNotFound"));

        if (old.isExpired() || !old.getMember().getId().equals(dto.getMemberId())) {
            throw new IllegalArgumentException("invalidOrExpiredRefreshToken");
        }

        old.changeExpiresAt(LocalDateTime.now());
        refreshTokenRepository.save(old);

        String newToken = UUID.randomUUID().toString();
        LocalDateTime newExpiry = LocalDateTime.now().plusMinutes(dto.getNewExpiryMinutes());
        RefreshToken next = RefreshToken.builder()
                .member(Member.builder().id(dto.getMemberId()).build())
                .token(newToken)
                .expiresAt(newExpiry)
                .build();

        RefreshToken saved = refreshTokenRepository.save(next);
        return entityToDTO(saved);
    }

    @Override
    public void deleteByMemberId(Long memberId) {
        refreshTokenRepository.findByMemberId(memberId)
                .ifPresent(refreshTokenRepository::delete);
    }

    private RefreshToken dtoToEntity(RefreshTokenCreateRequestDTO dto) {
        return RefreshToken.builder()
                .member(Member.builder().id(dto.getMemberId()).build())
                .token(dto.getToken())
                .expiresAt(dto.getExpiresAt())
                .build();
    }

}
