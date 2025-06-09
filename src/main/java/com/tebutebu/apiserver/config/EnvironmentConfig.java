package com.tebutebu.apiserver.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 환경 설정 클래스
 * .env 파일을 읽어서 시스템 환경변수로 로드합니다.
 */
@Configuration
@Log4j2
public class EnvironmentConfig {

    /**
     * 애플리케이션 시작 시 .env 파일을 로드합니다.
     */
    @PostConstruct
    public void loadEnvironmentVariables() {
        Path envPath = Paths.get(".env");
        
        // 현재 디렉토리에 .env 파일이 없으면 /app/.env 경로 확인
        if (!Files.exists(envPath)) {
            envPath = Paths.get("/app/.env");
        }
        
        if (Files.exists(envPath)) {
            try {
                log.info("🔧 .env 파일을 환경변수로 로드 중: {}", envPath.toAbsolutePath());
                
                List<String> lines = Files.readAllLines(envPath);
                int loadedCount = 0;
                
                for (String line : lines) {
                    line = line.trim();
                    
                    // 빈 라인이나 주석 라인 무시
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    
                    // KEY=VALUE 형식 파싱
                    int equalIndex = line.indexOf('=');
                    if (equalIndex > 0) {
                        String key = line.substring(0, equalIndex).trim();
                        String value = line.substring(equalIndex + 1).trim();
                        
                        // 따옴표 제거
                        if ((value.startsWith("\"") && value.endsWith("\"")) ||
                            (value.startsWith("'") && value.endsWith("'"))) {
                            value = value.substring(1, value.length() - 1);
                        }
                        
                        // 시스템 환경변수로 설정 (기존 환경변수가 없는 경우에만)
                        if (System.getProperty(key) == null && System.getenv(key) == null) {
                            System.setProperty(key, value);
                            loadedCount++;
                            
                            // 중요한 설정들만 로깅 (보안 정보 제외)
                            if (key.contains("CLIENT_ID") || key.contains("REDIRECT_URI") || 
                                key.contains("PROFILES_ACTIVE") || key.contains("FRONTEND")) {
                                if (key.contains("SECRET") || key.contains("PASSWORD")) {
                                    log.info("✅ {} = {}...", key, value.length() > 0 ? value.substring(0, Math.min(4, value.length())) + "***" : "");
                                } else {
                                    log.info("✅ {} = {}", key, value);
                                }
                            }
                        }
                    }
                }
                
                log.info("🎉 .env 파일에서 {} 개의 환경변수를 로드했습니다!", loadedCount);
                
                // 중요한 설정값들 확인
                logImportantSettings();
                
            } catch (IOException e) {
                log.error("❌ .env 파일 읽기 실패: {}", e.getMessage());
            }
        } else {
            log.warn("⚠️ .env 파일을 찾을 수 없습니다. 경로: {}", envPath.toAbsolutePath());
        }
    }
    
    /**
     * 중요한 설정값들을 로깅합니다.
     */
    private void logImportantSettings() {
        log.info("📋 주요 환경변수 확인:");
        log.info("  - SPRING_PROFILES_ACTIVE: {}", getPropertyOrEnv("SPRING_PROFILES_ACTIVE", "NOT_SET"));
        log.info("  - KAKAO_CLIENT_ID: {}...", maskValue(getPropertyOrEnv("KAKAO_CLIENT_ID", "NOT_SET")));
        log.info("  - KAKAO_REDIRECT_URI: {}", getPropertyOrEnv("KAKAO_REDIRECT_URI", "NOT_SET"));
        log.info("  - LOCAL_FRONTEND_REDIRECT_URI: {}", getPropertyOrEnv("LOCAL_FRONTEND_REDIRECT_URI", "NOT_SET"));
    }
    
    /**
     * 시스템 프로퍼티 또는 환경변수에서 값을 가져옵니다.
     */
    private String getPropertyOrEnv(String key, String defaultValue) {
        String value = System.getProperty(key);
        if (value == null) {
            value = System.getenv(key);
        }
        return value != null ? value : defaultValue;
    }
    
    /**
     * 보안상 중요한 값들을 마스킹합니다.
     */
    private String maskValue(String value) {
        if (value == null || value.equals("NOT_SET") || value.length() < 4) {
            return value;
        }
        return value.substring(0, 4) + "***";
    }
} 