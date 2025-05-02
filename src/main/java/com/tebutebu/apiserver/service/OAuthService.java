package com.tebutebu.apiserver.service;

import com.tebutebu.apiserver.domain.OAuth;
import com.tebutebu.apiserver.dto.oauth.request.OAuthCreateRequestDTO;
import com.tebutebu.apiserver.dto.oauth.response.OAuthResponseDTO;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface OAuthService {

    Long register(OAuthCreateRequestDTO dto);

    OAuth dtoToEntity(OAuthCreateRequestDTO dto);

    default OAuthResponseDTO entityToDTO(OAuth oAuth) {
        return OAuthResponseDTO.builder()
                .memberId(oAuth.getMember().getId())
                .provider(oAuth.getProvider())
                .providerId(oAuth.getProviderId())
                .build();
    }

}
