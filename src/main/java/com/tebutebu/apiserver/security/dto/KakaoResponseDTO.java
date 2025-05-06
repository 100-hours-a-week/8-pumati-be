package com.tebutebu.apiserver.security.dto;

import java.util.Map;

public class KakaoResponseDTO implements OAuth2ResponseDTO {

    private final Map<String, Object> attributes;

    public KakaoResponseDTO(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getProviderId() {
        return attributes.get("id").toString();
    }

    @Override
    public String getEmail() {
        Object accountObj = attributes.get("kakao_account");
        if (!(accountObj instanceof Map<?,?> account)) {
            return null;
        }
        Object email = account.get("email");
        return email != null ? email.toString() : null;
    }

}
