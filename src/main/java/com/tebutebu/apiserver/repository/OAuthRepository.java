package com.tebutebu.apiserver.repository;

import com.tebutebu.apiserver.domain.OAuth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OAuthRepository extends JpaRepository<OAuth, Long> {

    Optional<OAuth> findByMemberId(Long memberId);

    boolean existsByMemberId(Long memberId);

    boolean existsByProviderAndProviderId(String provider, String providerId);

}
