package com.tebutebu.apiserver.security.filter;

import com.google.gson.Gson;
import com.tebutebu.apiserver.domain.Member;
import com.tebutebu.apiserver.domain.MemberRole;
import com.tebutebu.apiserver.domain.Team;
import com.tebutebu.apiserver.security.dto.CustomOAuth2User;
import com.tebutebu.apiserver.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Log4j2
public class JWTCheckFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }

        String path = request.getRequestURI();
        if (path.startsWith("/oauth2/authorization/")) {
            return true;
        }

        if (request.getMethod().equals("GET")) {
            if (path.equals("/") || path.equals("/docs") || path.startsWith("/api/oauth/")
                    || path.startsWith("/api/teams") || path.startsWith("/api/projects") || path.startsWith("/api/comments")) {
                return true;
            }
        }

        if (request.getMethod().equals("POST")) {
            if (path.equals("/api/members/social") || path.startsWith("/api/auth/") || path.equals("/api/pre-signed-url") || path.equals("/api/pre-signed-urls") || path.equals("/api/projects/snapshot")) {
                return true;
            }
        }

        if (request.getMethod().equals("PUT")) {
            if (path.equals("/api/auth/tokens")) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException {
        String authHeaderStr = request.getHeader("Authorization");
        try {
            // Bearer accessToken...
            String accessToken = authHeaderStr.substring(7);
            Map<String, Object> claims = JWTUtil.validateToken(accessToken);
            log.info("JWT claims: " + claims);

            Long memberId = ((Number) claims.get("id")).longValue();

            Object rawTeamId = claims.get("teamId");
            Long teamId = (rawTeamId instanceof Number)
                    ? ((Number) rawTeamId).longValue()
                    : null;

            String email = (String) claims.get("email");
            String nickname = (String) claims.get("nickname");
            String profileImageUrl = (String) claims.get("profileImageUrl");
            String role = (String) claims.get("role");
            MemberRole memberRole = MemberRole.valueOf(role);

            Team team = null;
            if (teamId != null) {
                team = Team.builder()
                        .id(teamId)
                        .build();
            }

            Member member = Member.builder()
                    .id(memberId)
                    .team(team)
                    .email(email)
                    .nickname(nickname)
                    .profileImageUrl(profileImageUrl)
                    .role(memberRole)
                    .build();
            log.info("Member: " + member);

            CustomOAuth2User customOAuth2User = new CustomOAuth2User(member);
            log.info("CustomOAuth2User: " + customOAuth2User);

            UsernamePasswordAuthenticationToken authenticationToken
                    = new UsernamePasswordAuthenticationToken(customOAuth2User, "", customOAuth2User.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            filterChain.doFilter(request, response);
        } catch(Exception e) {
            log.warn("Invalid JWT: {}", e.getMessage());
            SecurityContextHolder.clearContext();

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json; charset=UTF-8");
            String json = new Gson().toJson(Map.of("message", "invalidToken"));
            response.getWriter().write(json);
        }
    }
}
