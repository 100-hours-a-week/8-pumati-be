package com.tebutebu.apiserver.security.service;

import com.tebutebu.apiserver.domain.Member;
import com.tebutebu.apiserver.repository.MemberRepository;
import com.tebutebu.apiserver.security.dto.CustomOAuth2User;
import com.tebutebu.apiserver.security.dto.KakaoResponseDTO;
import com.tebutebu.apiserver.security.dto.OAuth2ResponseDTO;
import com.tebutebu.apiserver.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Log4j2
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.info("OAuth2User: " + oAuth2User.getAttributes());

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        log.info("registrationId: " + registrationId);

        OAuth2ResponseDTO oAuth2ResponseDTO = null;
        if (registrationId.equals("kakao")) {
            oAuth2ResponseDTO = new KakaoResponseDTO(oAuth2User.getAttributes());
        } else {
            return null;
        }

        String email = oAuth2ResponseDTO.getEmail();
        Member member = memberRepository.findByEmail(email);
        if (member == null) {
            Map<String,Object> claims = Map.of(
                    "provider", oAuth2ResponseDTO.getProvider(),
                    "providerId", oAuth2ResponseDTO.getProviderId(),
                    "email", email
            );

            String signupToken = JWTUtil.generateToken(claims, 60 * 10);
            OAuth2Error error = new OAuth2Error(
                    "additionalInfoRequired",
                    signupToken,
                    null
            );
            throw new OAuth2AuthenticationException(error, error.toString());
        }

        return new CustomOAuth2User(member);
    }

}
