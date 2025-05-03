package com.tebutebu.apiserver.security.dto;

import com.tebutebu.apiserver.domain.Member;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Getter
public class CustomOAuth2User implements OAuth2User {

    private final Member member;

    private final Long memberId;

    public CustomOAuth2User(Member member) {
        this.member = member;
        this.memberId = member.getId();
    }

    @Override
    public Map<String, Object> getAttributes() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", member.getId());

        Long teamId = member.getTeam() != null ? member.getTeam().getId() : null;
        attributes.put("teamId", teamId);

        attributes.put("email", member.getEmail());
        attributes.put("name", member.getName());
        attributes.put("nickname", member.getNickname());
        attributes.put("profileImageUrl", member.getProfileImageUrl());
        attributes.put("role", member.getRole().name());
        return Collections.unmodifiableMap(attributes);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + member.getRole()));
    }

    @Override
    public String getName() {
        // OAuth2 principal name, fallback to email
        return member.getEmail();
    }

}
