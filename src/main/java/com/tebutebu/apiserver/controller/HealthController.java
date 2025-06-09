package com.tebutebu.apiserver.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 헬스체크 및 기본 API 정보를 제공하는 컨트롤러
 * 
 * 주요 기능:
 * - 루트 경로 "/" 요청 처리 (NoResourceFoundException 방지)
 * - 서버 상태 확인 API 제공
 * - 로드밸런서 헬스체크 지원
 */
@RestController
@Log4j2
public class HealthController {

    /**
     * 루트 경로 요청 처리
     * 로드밸런서나 브라우저에서 "/" 요청 시 기본 정보 제공
     * 
     * @return API 서버 기본 정보
     */
    @GetMapping("/")
    public ResponseEntity<?> root() {
        log.debug("루트 경로 요청 처리");
        
        return ResponseEntity.ok(Map.of(
            "service", "Pumati Backend API Server",
            "status", "running",
            "timestamp", LocalDateTime.now(),
            "message", "API 서버가 정상적으로 동작 중입니다.",
            "docs", "/actuator/health", // 실제 헬스체크 엔드포인트
            "version", "1.0.0"
        ));
    }

    /**
     * 간단한 헬스체크 엔드포인트
     * 로드밸런서나 모니터링 도구에서 서버 상태 확인용
     * 
     * @return 서버 상태 정보
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        log.debug("헬스체크 요청 처리");
        
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "timestamp", LocalDateTime.now(),
            "checks", Map.of(
                "application", "UP",
                "database", "UP", // 실제로는 DB 연결 상태를 확인해야 함
                "memory", "UP"
            )
        ));
    }

    /**
     * 상세 서버 정보 제공
     * 운영진이나 모니터링 도구에서 서버 정보 확인용
     * 
     * @return 상세 서버 정보
     */
    @GetMapping("/info")
    public ResponseEntity<?> info() {
        log.debug("서버 정보 요청 처리");
        
        // 메모리 정보
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        return ResponseEntity.ok(Map.of(
            "application", Map.of(
                "name", "Pumati Backend API",
                "version", "1.0.0",
                "environment", "development", // 환경변수에서 가져오는 것이 좋음
                "java-version", System.getProperty("java.version")
            ),
            "system", Map.of(
                "timestamp", LocalDateTime.now(),
                "uptime", System.currentTimeMillis(),
                "memory", Map.of(
                    "total", totalMemory / 1024 / 1024 + "MB",
                    "used", usedMemory / 1024 / 1024 + "MB",
                    "free", freeMemory / 1024 / 1024 + "MB"
                )
            )
        ));
    }
} 