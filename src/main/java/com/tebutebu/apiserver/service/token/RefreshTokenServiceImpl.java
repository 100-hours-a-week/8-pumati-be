package com.tebutebu.apiserver.service.token;

import com.tebutebu.apiserver.domain.Member;
import com.tebutebu.apiserver.domain.RefreshToken;
import com.tebutebu.apiserver.dto.token.request.RefreshTokenCreateRequestDTO;
import com.tebutebu.apiserver.dto.token.request.RefreshTokenRotateRequestDTO;
import com.tebutebu.apiserver.dto.token.response.RefreshTokenResponseDTO;
import com.tebutebu.apiserver.global.errorcode.BusinessErrorCode;
import com.tebutebu.apiserver.global.exception.BusinessException;
import com.tebutebu.apiserver.repository.RefreshTokenRepository;
import com.tebutebu.apiserver.service.token.redis.RefreshTokenRedisService;
import com.tebutebu.apiserver.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Log4j2
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    private final RefreshTokenRedisService refreshTokenRedisService;

    @Value("${spring.jwt.refresh-token.expiration}")
    private int refreshTokenExpiration;

    @Override
    public void createOrUpdateRefreshToken(RefreshTokenCreateRequestDTO dto) {
        if (dto.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(BusinessErrorCode.INVALID_REFRESH_TOKEN_EXPIRY);
        }

        RefreshToken token = refreshTokenRepository.findByMemberId(dto.getMemberId())
                .map(existing -> {
                    existing.changeToken(dto.getToken());
                    existing.changeExpiresAt(dto.getExpiresAt());
                    return existing;
                })
                .orElseGet(() -> dtoToEntity(dto));

        refreshTokenRepository.save(token);
        refreshTokenRedisService.save(dto.getToken(), dto.getMemberId(), refreshTokenExpiration);
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
        Long memberId = refreshTokenRedisService.getMemberId(token);
        if (memberId != null) {
            return RefreshTokenResponseDTO.builder()
                    .token(token)
                    .memberId(memberId)
                    .expiresAt(LocalDateTime.now().plusMinutes(refreshTokenExpiration))
                    .build();
        }

        RefreshToken entity = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.REFRESH_TOKEN_NOT_FOUND));
        refreshTokenRedisService.save(entity.getToken(), entity.getMember().getId(), refreshTokenExpiration);

        return entityToDTO(entity);
    }

    @Override
    public RefreshTokenResponseDTO rotateToken(RefreshTokenRotateRequestDTO dto) {
        RefreshToken old = refreshTokenRepository.findByToken(dto.getOldToken())
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.REFRESH_TOKEN_NOT_FOUND));

        if (old.isExpired() || !old.getMember().getId().equals(dto.getMemberId())) {
            throw new BusinessException(BusinessErrorCode.INVALID_REFRESH_TOKEN);
        }

        String newToken = JWTUtil.generateRefreshToken(dto.getMemberId());

        old.changeToken(newToken);
        old.changeExpiresAt(LocalDateTime.now().plusMinutes(dto.getNewExpiryMinutes()));
        RefreshToken savedToken = refreshTokenRepository.save(old);

        refreshTokenRedisService.delete(dto.getOldToken());
        refreshTokenRedisService.save(newToken, dto.getMemberId(), dto.getNewExpiryMinutes());

        return entityToDTO(savedToken);
    }

    @Override
    public void deleteByMemberId(Long memberId) {
        refreshTokenRepository.findByMemberId(memberId)
                .ifPresent(token -> {
                    refreshTokenRepository.delete(token);
                    refreshTokenRedisService.delete(token.getToken());
                });
    }

    private RefreshToken dtoToEntity(RefreshTokenCreateRequestDTO dto) {
        return RefreshToken.builder()
                .member(Member.builder().id(dto.getMemberId()).build())
                .token(dto.getToken())
                .expiresAt(dto.getExpiresAt())
                .build();
    }

}
