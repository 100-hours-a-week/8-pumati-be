# ==============================================================================
# Spring Boot Application Dockerfile (개발 환경용)
# Java 21, Spring Boot 3.4.5 기반
# ==============================================================================

# 1단계: 베이스 이미지 (OpenJDK 21 슬림 버전 사용)
FROM openjdk:21-slim

# 메타데이터 설정
LABEL maintainer="jay,jacky"
LABEL description="Pumati Backend API Server"
LABEL version="1.0"

# 작업 디렉토리 설정
WORKDIR /app

# 시스템 패키지 업데이트 및 필요한 도구 설치
RUN apt-get update && \
    apt-get install -y \
        curl \
        wget \
        ca-certificates \
        tzdata && \
    # 타임존을 서울로 설정
    ln -sf /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone && \
    # Elastic APM Java 에이전트 다운로드 (소스 코드 변경 없이 모니터링 추가)
    curl -L -o /opt/elastic-apm-agent.jar \
        https://search.maven.org/remotecontent?filepath=co/elastic/apm/elastic-apm-agent/1.44.0/elastic-apm-agent-1.44.0.jar && \
    # 캐시 정리
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# 애플리케이션 실행을 위한 사용자 생성 (보안 강화)
RUN groupadd -r appuser && \
    useradd -r -g appuser -s /bin/false appuser

# 애플리케이션 JAR 파일 복사
# Gradle 빌드 결과물인 JAR 파일을 컨테이너로 복사
COPY build/libs/*.jar app.jar

# 환경 변수 파일 복사 (Jenkins에서 생성된 .env 파일)
COPY .env /app/.env

# JAR 파일 및 APM 에이전트 권한 설정
RUN chown appuser:appuser app.jar && \
    chown appuser:appuser .env && \
    chown appuser:appuser /opt/elastic-apm-agent.jar && \
    chmod 644 app.jar && \
    chmod 600 .env && \
    chmod 644 /opt/elastic-apm-agent.jar

# 애플리케이션 포트 노출 (Spring Boot 기본 포트)
EXPOSE 8080

# 헬스체크 설정 (애플리케이션 상태 확인)
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# 애플리케이션 사용자로 전환
USER appuser

# ==============================================================================
# JVM 성능 최적화 옵션 설정
# ==============================================================================
# 컨테이너 환경에서 Spring Boot 애플리케이션의 성능과 안정성을 위한 JVM 튜닝
# 이 설정들은 컨테이너 실행 시 환경 변수로 오버라이드 가능합니다.
ENV JAVA_OPTS="-Xms512m \
-Xmx1g \
-XX:+UseG1GC \
-XX:G1HeapRegionSize=16m \
-XX:+UseStringDeduplication"

# 각 JVM 옵션 상세 설명:
# -Xms512m: 
#   ├─ JVM 시작 시 초기 힙 메모리를 512MB로 설정
#   ├─ 장점: 애플리케이션 시작 시간 단축 (메모리 미리 할당)
#   └─ 효과: 초기 메모리 부족으로 인한 지연 방지
#
# -Xmx1g:
#   ├─ JVM이 사용할 수 있는 최대 힙 메모리를 1GB로 제한
#   ├─ 장점: 컨테이너 메모리 사용량 예측 가능, OOM 방지
#   └─ 중요: Kubernetes/Docker 리소스 제한과 일치시켜야 함
#
# -XX:+UseG1GC:
#   ├─ G1 가비지 컬렉터 사용 (Garbage First GC)
#   ├─ 장점: 짧은 중단 시간 (Low Latency), 대용량 힙 최적화
#   ├─ 기본값 대비: Parallel GC보다 응답 시간 일관성 향상
#   └─ 적합: 웹 애플리케이션, 실시간 서비스
#
# -XX:G1HeapRegionSize=16m:
#   ├─ G1GC에서 힙을 16MB 단위의 리전으로 분할
#   ├─ 장점: 메모리 단편화 감소, 효율적인 메모리 관리
#   ├─ 기본값: 1MB-32MB (힙 크기에 따라 자동 결정)
#   └─ 최적화: 1GB 힙에서는 16MB가 적절한 크기
#
# -XX:+UseStringDeduplication:
#   ├─ 중복된 String 객체를 메모리에서 제거
#   ├─ 장점: 메모리 사용량 20-30% 절약 가능
#   ├─ 대상: 동일한 내용의 문자열들 (JSON, 로그 메시지 등)
#   └─ 효과: 대량의 문자열 처리 시 메모리 효율성 증대

# ==============================================================================
# Spring Boot 환경 설정
# ==============================================================================
# Spring Boot 프로파일 설정 (기본값: dev)
# 컨테이너 실행 시 -e SPRING_PROFILES_ACTIVE=prod 등으로 변경 가능
ENV SPRING_PROFILES_ACTIVE=dev

# ==============================================================================
# Elastic APM 에이전트 환경 설정 (소스 코드 변경 없이 모니터링 추가)
# ==============================================================================
# APM 서비스 이름 (Kibana에서 표시될 애플리케이션 이름)
ENV ELASTIC_APM_SERVICE_NAME=pumati-backend

# APM 환경 설정 (dev, staging, prod 등)
ENV ELASTIC_APM_ENVIRONMENT=dev

# APM 서버 URL (Kubernetes 내부 서비스명)
ENV ELASTIC_APM_SERVER_URL=http://apm-server-apm-server.elk-system.svc.cluster.local:8200

# APM 시크릿 토큰 (환경 변수로 오버라이드 가능)
ENV ELASTIC_APM_SECRET_TOKEN=""

# APM 로그 레벨 설정
ENV ELASTIC_APM_LOG_LEVEL=INFO

# APM 샘플링 비율 (1.0 = 100%, 0.1 = 10%)
ENV ELASTIC_APM_TRANSACTION_SAMPLE_RATE=1.0

# APM 활성화 여부 (true/false)
ENV ELASTIC_APM_ENABLED=true

# APM 애플리케이션 패키지 설정 (성능 최적화)
ENV ELASTIC_APM_APPLICATION_PACKAGES=com.tebutebu.apiserver

# ==============================================================================
# 애플리케이션 실행 및 환경 변수 로딩 (APM 에이전트 포함)
# ==============================================================================
# 복합 명령어를 통한 안전하고 효율적인 애플리케이션 시작 (APM 모니터링 활성화)
ENTRYPOINT ["sh", "-c", "set -a && . /app/.env && set +a && exec java $JAVA_OPTS -javaagent:/opt/elastic-apm-agent.jar -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE -jar app.jar"]

# ENTRYPOINT 명령어 상세 분석 (APM 에이전트 포함):
#
# "sh -c": 
#   ├─ 쉘을 통해 복합 명령어 실행
#   └─ 환경 변수 로딩과 Java 실행을 순차적으로 처리
#
# "set -a":
#   ├─ 이후 설정되는 모든 변수를 자동으로 export (환경 변수화)
#   ├─ .env 파일의 변수들이 Java 프로세스에서 접근 가능하게 됨
#   └─ 예: DB_HOST=localhost → Java에서 System.getenv("DB_HOST") 가능
#
# ". /app/.env":
#   ├─ .env 파일의 내용을 현재 쉘에 로드 (source와 동일)
#   ├─ 형식: KEY=VALUE 형태의 환경 변수들
#   └─ 예: spring.datasource.url=${DB_HOST}, ELASTIC_APM_SECRET_TOKEN=xxx 등
#
# "set +a":
#   ├─ auto-export 기능 비활성화 (보안 강화)
#   └─ 이후 설정되는 변수들은 명시적으로 export 해야 함
#
# "exec java":
#   ├─ 현재 쉘 프로세스를 Java 프로세스로 완전히 교체
#   ├─ 장점: PID 1로 Java가 실행됨 (컨테이너 신호 처리 향상)
#   ├─ 메모리 효율: 쉘 프로세스가 메모리에 남지 않음
#   └─ Docker 종료 신호 (SIGTERM)가 Java에 직접 전달됨
#
# "$JAVA_OPTS":
#   ├─ 위에서 설정한 JVM 튜닝 옵션들 적용
#   ├─ 실행 시 환경 변수로 오버라이드 가능
#   └─ 예: docker run -e JAVA_OPTS="-Xmx2g" 로 메모리 변경 가능
#
# "-javaagent:/opt/elastic-apm-agent.jar":
#   ├─ Elastic APM Java 에이전트를 JVM에 연결 (소스 코드 변경 없음!)
#   ├─ 애플리케이션 시작 시 자동으로 계측(instrumentation) 수행
#   ├─ Spring Boot, JPA, HTTP 요청/응답 등을 자동으로 모니터링
#   ├─ 환경 변수(ELASTIC_APM_*)를 통해 APM 서버로 데이터 전송
#   └─ 성능 영향: 일반적으로 5% 미만의 오버헤드
#
# "-Dspring.profiles.active=$SPRING_PROFILES_ACTIVE":
#   ├─ Spring Boot 프로파일을 JVM 시스템 프로퍼티로 설정
#   ├─ application-dev.yml, application-prod.yml 등 선택
#   └─ 실행 시 변경 가능: docker run -e SPRING_PROFILES_ACTIVE=prod
#
# "-jar app.jar":
#   ├─ Spring Boot Fat JAR 실행
#   └─ 모든 의존성이 포함된 실행 가능한 JAR 파일

# ==============================================================================
# 컨테이너 실행 예시:
# docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=dev pumati-be:latest
# ==============================================================================
