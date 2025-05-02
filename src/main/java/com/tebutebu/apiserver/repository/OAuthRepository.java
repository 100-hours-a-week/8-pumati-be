package com.tebutebu.apiserver.repository;

import com.tebutebu.apiserver.domain.OAuth;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuthRepository extends JpaRepository<OAuth, Long> {

    boolean existsByMemberId(Long memberId);

    boolean existsByProviderAndProviderId(String provider, String providerId);

}
