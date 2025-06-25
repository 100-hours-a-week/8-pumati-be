package com.tebutebu.apiserver.integration.redis;

import com.github.javafaker.Faker;
import com.tebutebu.apiserver.domain.RefreshToken;
import com.tebutebu.apiserver.dto.member.request.MemberOAuthSignupRequestDTO;
import com.tebutebu.apiserver.dto.member.response.MemberSignupResponseDTO;
import com.tebutebu.apiserver.dto.token.request.RefreshTokenCreateRequestDTO;
import com.tebutebu.apiserver.dto.token.response.RefreshTokenResponseDTO;
import com.tebutebu.apiserver.global.exception.BusinessException;
import com.tebutebu.apiserver.infrastructure.redis.util.RefreshTokenRedisKeyUtil;
import com.tebutebu.apiserver.repository.MemberRepository;
import com.tebutebu.apiserver.repository.OAuthRepository;
import com.tebutebu.apiserver.repository.RefreshTokenRepository;
import com.tebutebu.apiserver.service.member.MemberService;
import com.tebutebu.apiserver.service.token.RefreshTokenService;
import com.tebutebu.apiserver.util.JWTUtil;
import jakarta.servlet.http.Cookie;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Log4j2
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("RefreshTokenService 통합 테스트 (RDB + Redis)")
class RefreshTokenIntegrationTest {

    private static final String REFRESH_COOKIE_NAME = "refreshToken";

    private static final String OAUTH_PROVIDER = "kakao";

    private static final long TTL_MINUTES = 10;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private OAuthRepository oauthRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberService memberService;

    @Autowired
    private RedisTemplate<String, Long> longRedisTemplate;

    @Autowired
    private RefreshTokenRedisKeyUtil redisKeyUtil;

    private final Faker faker = new Faker();

    private final Faker koreanFaker = new Faker(new Locale("ko"));

    private Long memberId;

    private String testRefreshToken;

    private String redisKey;

    @BeforeEach
    void setup() {
        refreshTokenRepository.deleteAll();
        oauthRepository.deleteAll();
        memberRepository.deleteAll();
        Objects.requireNonNull(longRedisTemplate.getConnectionFactory()).getConnection().flushAll();

        String email = faker.internet().emailAddress();
        String providerId = UUID.randomUUID().toString();
        String name = koreanFaker.name().fullName();
        String nickname = faker.name().firstName();

        String signupToken = JWTUtil.generateToken(Map.of(
                "provider", OAUTH_PROVIDER,
                "providerId", providerId,
                "email", email
        ), TTL_MINUTES);

        MemberOAuthSignupRequestDTO signupDto = MemberOAuthSignupRequestDTO.builder()
                .signupToken(signupToken)
                .name(name)
                .nickname(nickname)
                .build();

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        MemberSignupResponseDTO signupResponse = memberService.registerOAuthUser(signupDto, request, response);
        memberId = signupResponse.getId();

        for (Cookie cookie : response.getCookies()) {
            if (REFRESH_COOKIE_NAME.equals(cookie.getName())) {
                testRefreshToken = cookie.getValue();
                break;
            }
        }

        redisKey = redisKeyUtil.toRedisKey(testRefreshToken);
    }

    @Nested
    @DisplayName("RefreshToken 생성 관련")
    class Create {

        @Test
        @DisplayName("refreshToken 생성 시 RDB와 Redis에 저장")
        void savesToRdbAndRedis() {
            // given
            RefreshTokenCreateRequestDTO dto = RefreshTokenCreateRequestDTO.builder()
                    .memberId(memberId)
                    .token(testRefreshToken)
                    .expiresAt(LocalDateTime.now().plusMinutes(TTL_MINUTES))
                    .build();

            // when
            refreshTokenService.createOrUpdateRefreshToken(dto);

            // then: RDB 저장 여부 확인
            String storedToken = refreshTokenRepository.findByMemberId(memberId)
                    .map(RefreshToken::getToken)
                    .orElseThrow();
            assertThat(storedToken).isEqualTo(testRefreshToken);

            // then: Redis 저장 여부 확인
            Long redisValue = longRedisTemplate.opsForValue().get(redisKey);
            assertThat(redisValue).isEqualTo(memberId);
        }

        @Test
        @DisplayName("memberId가 null이면 예외 발생")
        void throwsWhenMemberIdIsNull() {
            // given
            RefreshTokenCreateRequestDTO dto = RefreshTokenCreateRequestDTO.builder()
                    .memberId(null)
                    .token(testRefreshToken)
                    .expiresAt(LocalDateTime.now().plusMinutes(TTL_MINUTES))
                    .build();

            // when & then
            assertThrows(InvalidDataAccessApiUsageException.class, () -> {
                refreshTokenService.createOrUpdateRefreshToken(dto);
            });
        }

        @Test
        @DisplayName("token 값이 null이면 예외 발생")
        void throwsWhenTokenIsNull() {
            // given
            RefreshTokenCreateRequestDTO dto = RefreshTokenCreateRequestDTO.builder()
                    .memberId(memberId)
                    .token(null)
                    .expiresAt(LocalDateTime.now().plusMinutes(TTL_MINUTES))
                    .build();

            // when & then
            assertThrows(DataIntegrityViolationException.class, () -> {
                refreshTokenService.createOrUpdateRefreshToken(dto);
            });
        }
    }

    @Nested
    @DisplayName("RefreshToken 조회 관련")
    class Read {

        @Test
        @DisplayName("Redis에 존재하는 refreshToken으로 memberId 조회")
        void readsFromRedisIfPresent() {
            // given
            longRedisTemplate.opsForValue().set(redisKey, memberId, TTL_MINUTES, TimeUnit.MINUTES);

            // when
            RefreshTokenResponseDTO dto = refreshTokenService.findByToken(testRefreshToken);

            // then
            assertThat(dto.getMemberId()).isEqualTo(memberId);
        }

        @Test
        @DisplayName("존재하지 않는 refreshToken 조회 시 예외 발생")
        void throwsWhenTokenNotFound() {
            // given
            String nonexistentToken = "invalid.token.value";

            // when & then
            assertThrows(BusinessException.class, () -> {
                refreshTokenService.findByToken(nonexistentToken);
            });
        }
    }

    @Nested
    @DisplayName("RefreshToken 갱신 관련")
    class Update {

        @Test
        @DisplayName("기존 refreshToken을 새로운 값으로 갱신하면 RDB와 Redis 모두 반영")
        void updatesExistingRefreshToken() {
            // given
            refreshTokenService.persistRefreshToken(memberId, testRefreshToken);
            String newRefreshToken = UUID.randomUUID().toString();
            String newRedisKey = redisKeyUtil.toRedisKey(newRefreshToken);

            RefreshTokenCreateRequestDTO updateDto = RefreshTokenCreateRequestDTO.builder()
                    .memberId(memberId)
                    .token(newRefreshToken)
                    .expiresAt(LocalDateTime.now().plusMinutes(TTL_MINUTES))
                    .build();

            // when
            refreshTokenService.createOrUpdateRefreshToken(updateDto);

            // then: RDB에 갱신되었는지
            String updatedToken = refreshTokenRepository.findByMemberId(memberId)
                    .map(RefreshToken::getToken)
                    .orElseThrow();
            assertThat(updatedToken).isEqualTo(newRefreshToken);

            // then: Redis도 갱신되었는지
            assertThat(longRedisTemplate.opsForValue().get(newRedisKey)).isEqualTo(memberId);

            // 이전 Redis 키는 없어졌는지 (정책에 따라 optional)
            assertThat(longRedisTemplate.opsForValue().get(redisKey)).isNull();
        }

        @Test
        @DisplayName("expiresAt이 과거일 경우 예외 발생")
        void throwsWhenExpiresAtIsPast() {
            // given
            RefreshTokenCreateRequestDTO dto = RefreshTokenCreateRequestDTO.builder()
                    .memberId(memberId)
                    .token(testRefreshToken)
                    .expiresAt(LocalDateTime.now().minusMinutes(5))
                    .build();

            // when & then
            assertThrows(BusinessException.class, () -> {
                refreshTokenService.createOrUpdateRefreshToken(dto);
            });
        }
    }

    @Nested
    @DisplayName("RefreshToken 삭제 관련")
    class Delete {

        @Test
        @DisplayName("refreshToken 삭제 시 RDB와 Redis 모두 삭제")
        void deletesFromRdbAndRedis() {
            // given
            refreshTokenService.persistRefreshToken(memberId, testRefreshToken);

            // when
            refreshTokenService.deleteByMemberId(memberId);

            // then
            assertThat(refreshTokenRepository.findByMemberId(memberId)).isEmpty();
            assertThat(longRedisTemplate.opsForValue().get(redisKey)).isNull();
        }

        @Test
        @DisplayName("존재하지 않는 memberId로 삭제 시 예외 없이 무시")
        void deleteWithNonexistentMemberIdShouldNotThrow() {
            // given
            Long nonexistentId = 0L;

            // when & then
            Assertions.assertDoesNotThrow(() -> {
                refreshTokenService.deleteByMemberId(nonexistentId);
            });

            // then
            assertThat(refreshTokenRepository.findByMemberId(nonexistentId)).isEmpty();
        }
    }
}
