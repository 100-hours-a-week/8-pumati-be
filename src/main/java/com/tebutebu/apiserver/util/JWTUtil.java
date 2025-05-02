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

@Component
public class JWTUtil {

    private static String secretKey;

    @Value("${spring.jwt.secret}")
    public void setSecretKey(String secretKey) {
        JWTUtil.secretKey = secretKey;
    }

    public static String generateToken(Map<String, Object> valueMap, int min) {

        SecretKey key = null;

        try {
            key = Keys.hmacShaKeyFor(JWTUtil.secretKey.getBytes(StandardCharsets.UTF_8));
        } catch(Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        return Jwts.builder()
                .setHeader(Map.of("typ","JWT"))
                .setClaims(valueMap)
                .setIssuedAt(Date.from(ZonedDateTime.now().toInstant()))
                .setExpiration(Date.from(ZonedDateTime.now().plusMinutes(min).toInstant()))
                .signWith(key)
                .compact();
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
        } catch(MalformedJwtException malformedJwtException) {
            throw new CustomJWTException("MalFormed");
        } catch(ExpiredJwtException expiredJwtException) {
            throw new CustomJWTException("Expired");
        } catch(InvalidClaimException invalidClaimException) {
            throw new CustomJWTException("Invalid");
        } catch(JwtException jwtException) {
            throw new CustomJWTException("JWTError");
        } catch(Exception e) {
            throw new CustomJWTException("Error");
        }
        return claim;
    }

}
