#!/bin/bash

# =============================================================================
# Pumati Backend 애플리케이션 시작 스크립트
# .env 파일을 로드하고 Spring Boot 애플리케이션을 실행합니다.
# =============================================================================

echo "🔧 Pumati Backend 애플리케이션 시작 중..."
echo "📍 현재 작업 디렉토리: $(pwd)"
echo "👤 실행 사용자: $(whoami)"
echo "📅 시작 시간: $(date)"
echo ""

# .env 파일 존재 여부 확인
ENV_FILE="/app/.env"
if [ -f "$ENV_FILE" ]; then
    echo "📂 .env 파일 발견: ✅"
    echo "📊 .env 파일 정보:"
    echo "  - 파일 크기: $(ls -lh $ENV_FILE | awk '{print $5}')"
    echo "  - 줄 수: $(wc -l < $ENV_FILE)"
    echo "  - 권한: $(ls -l $ENV_FILE | awk '{print $1}')"
    echo ""
    
    echo "🔄 .env 파일에서 환경변수 로딩 중..."
    
    # .env 파일을 환경변수로 로드
    set -a  # 모든 변수를 자동으로 export
    source "$ENV_FILE"
    set +a  # auto-export 비활성화
    
    echo "✅ 환경변수 로딩 완료!"
    echo ""
    
    echo "📋 주요 환경변수 확인:"
    echo "  - SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE:-NOT_SET}"
    echo "  - KAKAO_CLIENT_ID: ${KAKAO_CLIENT_ID:0:8}..." 
    echo "  - KAKAO_REDIRECT_URI: ${KAKAO_REDIRECT_URI:-NOT_SET}"
    echo "  - LOCAL_FRONTEND_REDIRECT_URI: ${LOCAL_FRONTEND_REDIRECT_URI:-NOT_SET}"
    echo "  - DEV_DB_HOST: ${DEV_DB_HOST:-NOT_SET}"
    echo "  - DEV_DB_PORT: ${DEV_DB_PORT:-NOT_SET}"
    echo ""
    
    echo "🔍 Spring Boot 자동 매핑 확인:"
    echo "  - kakao.client.id → KAKAO_CLIENT_ID: ${KAKAO_CLIENT_ID:+매핑됨}"
    echo "  - kakao.redirect.uri → KAKAO_REDIRECT_URI: ${KAKAO_REDIRECT_URI:+매핑됨}"
    echo ""
    
    # 중요한 환경변수가 설정되어 있는지 검증
    MISSING_VARS=""
    
    if [ -z "$KAKAO_CLIENT_ID" ]; then
        MISSING_VARS="$MISSING_VARS KAKAO_CLIENT_ID"
    fi
    
    if [ -z "$KAKAO_REDIRECT_URI" ]; then
        MISSING_VARS="$MISSING_VARS KAKAO_REDIRECT_URI"
    fi
    
    if [ -z "$DEV_DB_HOST" ]; then
        MISSING_VARS="$MISSING_VARS DEV_DB_HOST"
    fi
    
    if [ -n "$MISSING_VARS" ]; then
        echo "⚠️  다음 중요한 환경변수가 설정되지 않았습니다:$MISSING_VARS"
        echo "   애플리케이션이 정상적으로 작동하지 않을 수 있습니다."
        echo ""
    else
        echo "✅ 모든 필수 환경변수가 설정되었습니다!"
        echo ""
    fi
    
else
    echo "❌ .env 파일을 찾을 수 없습니다: $ENV_FILE"
    echo ""
    echo "📂 /app 디렉토리 내용:"
    ls -la /app/
    echo ""
    echo "⚠️  환경변수 없이 기본 설정으로 실행합니다."
    echo ""
fi

# JVM 및 Spring Boot 설정 정보 출력
echo "🚀 Spring Boot 애플리케이션 시작 준비..."
echo "☕ Java 버전: $(java -version 2>&1 | head -n 1)"
echo "🏗️  JVM 옵션: ${JAVA_OPTS:-기본값 사용}"
echo "🔧 Spring 프로파일: ${SPRING_PROFILES_ACTIVE:-dev}"
echo "🎯 JAR 파일: $(ls -la /app/app.jar 2>/dev/null | awk '{print $5}' || echo '파일 없음')"
echo ""

echo "🎬 애플리케이션 실행 시작..."
echo "========================================"
echo ""

# Spring Boot 애플리케이션 실행
# exec을 사용하여 현재 프로세스를 Java 프로세스로 교체 (PID 1 유지)
exec java ${JAVA_OPTS} \
    -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-dev} \
    -Dfile.encoding=UTF-8 \
    -Duser.timezone=Asia/Seoul \
    -jar /app/app.jar 