package com.tebutebu.apiserver.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tebutebu.apiserver.security.filter.JWTCheckFilter;
import com.tebutebu.apiserver.security.handler.CustomAccessDeniedHandler;
import com.tebutebu.apiserver.security.handler.CustomLoginFailHandler;
import com.tebutebu.apiserver.security.handler.CustomLoginSuccessHandler;
import com.tebutebu.apiserver.security.handler.CustomLogoutHandler;
import com.tebutebu.apiserver.security.service.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@Log4j2
@RequiredArgsConstructor
public class CustomSecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;

    private final CustomLoginSuccessHandler loginSuccessHandler;

    private final CustomLoginFailHandler loginFailHandler;

    private final CustomLogoutHandler logoutHandler;

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.fromHierarchy(
                "ROLE_ADMIN > ROLE_TRAINEE\n" +
                        "ROLE_TRAINEE > ROLE_USER"
        );
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.csrf(AbstractHttpConfigurer::disable);
        http.formLogin(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);

        http.exceptionHandling(httpSecurityExceptionHandlingConfigurer -> {
            httpSecurityExceptionHandlingConfigurer.accessDeniedHandler(new CustomAccessDeniedHandler());
        });

        http.oauth2Login(oauth2 -> oauth2
                .redirectionEndpoint(redir -> redir
                        .baseUri("/api/oauth/{registrationId}")
                )
                .userInfoEndpoint(uie -> uie.userService(customOAuth2UserService))
                .successHandler(loginSuccessHandler)
                .failureHandler(loginFailHandler)
        );

        http.addFilterBefore(new JWTCheckFilter(), AuthorizationFilter.class);

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/teams").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/comments/ai/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/comments/ai/**").hasRole("ADMIN")
                .anyRequest().permitAll()
        );

        http.logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/api/auth/tokens", "DELETE"))
                .addLogoutHandler(logoutHandler)
                .logoutSuccessHandler((req, res, auth) -> {
                    res.setStatus(HttpServletResponse.SC_OK);
                    res.setContentType("application/json; charset=UTF-8");
                    new ObjectMapper().writeValue(res.getWriter(),
                            Map.of("message", "logoutSuccess")
                    );
                })
        );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
