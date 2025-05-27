package com.tebutebu.apiserver.service;

import com.tebutebu.apiserver.domain.OAuth;
import com.tebutebu.apiserver.domain.Member;
import com.tebutebu.apiserver.dto.oauth.request.OAuthCreateRequestDTO;
import com.tebutebu.apiserver.repository.OAuthRepository;
import com.tebutebu.apiserver.util.exception.CustomValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class OAuthServiceImpl implements OAuthService {

    @Value("${oauth.allowed-providers}")
    private String allowedProviders;

    private final OAuthRepository oAuthRepository;

    @Override
    public Long register(OAuthCreateRequestDTO dto) {

        if (oAuthRepository.existsByMemberId(dto.getMemberId())) {
            return dto.getMemberId();
        }

        if (oAuthRepository.existsByProviderAndProviderId(dto.getProvider(), dto.getProviderId())) {
            throw new CustomValidationException("oauthAlreadyExists");
        }

        OAuth oAuth = oAuthRepository.save(dtoToEntity(dto));
        return oAuth.getId();
    }

    @Override
    public void validateProvider(String provider) {
        List<String> allowedList = Arrays.asList(allowedProviders.split(","));
        if (!allowedList.contains(provider)) {
            throw new CustomValidationException("invalidProvider");
        }
    }

    @Override
    public void deleteByMemberId(Long memberId) {
        oAuthRepository.findByMemberId(memberId)
                .ifPresent(oAuthRepository::delete);
    }

    @Override
    public OAuth dtoToEntity(OAuthCreateRequestDTO dto) {
        return OAuth.builder()
                .member(Member.builder().id(dto.getMemberId()).build())
                .provider(dto.getProvider())
                .providerId(dto.getProviderId())
                .build();
    }

}
