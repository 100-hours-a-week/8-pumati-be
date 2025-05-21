package com.tebutebu.apiserver.util;

import com.tebutebu.apiserver.util.exception.CustomJWTException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.InvalidClaimException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
public class JWTUtil {

    private static String secretKey;

    private static long accessTokenExpireMin;

    private static long refreshTokenExpireMin;

    @Value("${spring.jwt.secret}")
    public void setSecretKey(String secretKey) {
        JWTUtil.secretKey = secretKey;
    }

    @Value("${spring.jwt.access-token.expiration}")
    public void setAccessTokenExpireMin(long minutes) {
        JWTUtil.accessTokenExpireMin = minutes;
    }

    @Value("${spring.jwt.refresh-token.expiration}")
    public void setRefreshTokenExpireMin(long minutes) {
        JWTUtil.refreshTokenExpireMin = minutes;
    }

    public static String generateToken(Map<String, Object> claims, long expireMin) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(Date.from(ZonedDateTime.now().plusMinutes(expireMin).toInstant()))
                .signWith(key)
                .compact();
    }

    public static String generateAccessToken(Map<String, Object> claims) {
        return generateToken(claims, accessTokenExpireMin);
    }

    public static String generateRefreshToken(Long memberId) {
        String jti = UUID.randomUUID().toString();
        Map<String, Object> claims = Map.of(
                "sub", memberId,
                "jti", jti,
                "typ", "refresh"
        );
        return generateToken(claims, refreshTokenExpireMin);
    }

    public static Map<String, Object> validateToken(String token) {
        Map<String, Object> claim;
        try {
            SecretKey key = Keys.hmacShaKeyFor(JWTUtil.secretKey.getBytes(StandardCharsets.UTF_8));
            claim = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (MalformedJwtException malformedJwtException) {
            throw new CustomJWTException("tokenMalformed");
        } catch (ExpiredJwtException expiredJwtException) {
            throw new CustomJWTException("tokenExpired");
        } catch (InvalidClaimException invalidClaimException) {
            throw new CustomJWTException("invalidClaim");
        } catch (JwtException jwtException) {
            throw new CustomJWTException("jwtException");
        } catch (Exception e) {
            throw new CustomJWTException(e.getMessage());
        }
        return claim;
    }

    public static SignupTokenRecord parseSignupToken(String signupToken) {
        var claims = validateToken(signupToken);
        return new SignupTokenRecord(
                (String) claims.get("provider"),
                (String) claims.get("providerId"),
                (String) claims.get("email")
        );
    }

}
