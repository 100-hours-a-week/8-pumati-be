### 8-pumati-be
[품앗이(Pumati)](https://github.com/100-hours-a-week/8-pumati-wiki/wiki)를 위한 백엔드 API 서버

### 설계 문서

- [기술 검토 및 선정](https://www.notion.so/juintination/1dd86f3c096e809181e0da937f9dde1c)
- [프로젝트 도메인 테크스펙](https://www.notion.so/juintination/1e086f3c096e80cd9492df62af59f318)

### Entity Relationship Diagram
- ErdCloud로 [ERD](https://www.erdcloud.com/d/jnEJjaPDkS2TSJryZ)를 작성하였습니다.
- Notion으로 [테이블 정의서](https://www.notion.so/juintination/1d786f3c096e805297a1cede038e3202)를 작성하였습니다.

### API Documentation
- Notion으로 [API Best Practice](https://www.notion.so/juintination/REST-API-Best-Practice-1db86f3c096e802eaf4ee2ee0dfae21c) 문서를 작성하였습니다.
- [API 문서](https://documenter.getpostman.com/view/32366655/2sB2j7eVGx)는 Postman으로 작성하였습니다.
  - [API 설계서](https://docs.google.com/spreadsheets/d/1qNipjPk-KIijezN7nLtT1a6lQQy5DS0XM7w0wLd3mrw/edit?gid=1878554884#gid=1878554884)는 Google Spreadsheets로 작성하였습니다.
- 프로젝트가 실행 중일 때 기본 URL을 통해 문서에 접근할 수 있습니다.

### 주요 기능

#### OAuth2 + JWT 기반 인증
- Kakao OAuth2 로그인과 JWT를 결합하여 **Stateless 인증 구조**를 설계했습니다.
- **백엔드가 OAuth 인증의 모든 책임을 단일하게 관리**합니다.  
  - 프론트엔드는 단순히 Kakao 인증 코드를 전달하고, 백엔드가 Access Token 교환, 사용자 정보 검증, 회원 등록, JWT 발급까지 전 과정을 처리합니다.
  - 이를 통해 **보안 위험을 최소화**하고, 인증 절차를 **서버 중심으로 일관성 있게 통제**할 수 있습니다.
  - [관련 정리 글](https://juintination.tistory.com/entry/OAuth2-Code-Grant%EC%97%90%EC%84%9C%EC%9D%98-%ED%94%84%EB%A1%A0%ED%8A%B8%EB%B0%B1%EC%97%94%EB%93%9C-%EC%B1%85%EC%9E%84-%EB%B6%84%EB%A6%AC%EC%99%80-JWT-%EB%B0%9C%EA%B8%89-%EA%B3%A0%EB%AF%BC%EA%B8%B0)
- 최초 로그인 시 추가 정보 입력이 필요한 사용자는 **임시 Signup Token**을 발급받아, 세션 없이 인증 단계를 이어갈 수 있습니다.
- Access Token은 헤더에, Refresh Token은 **HttpOnly 쿠키**에 저장하며, **RTR(Rotate Refresh Token)** 방식을 적용하여 보안성을 강화했습니다.
  - 매 요청마다 새로운 Refresh Token을 재발급함으로써 **토큰 탈취 위험을 최소화**했습니다.  
  - Refresh Token은 **HttpOnly 쿠키에 저장**되어 클라이언트 JavaScript에서 접근할 수 없으며, **XSS 공격에 대한 보안성을 강화**했습니다.  
  - 만료되거나 변조된 토큰은 **서버 검증 단계에서 즉시 무효화**됩니다.  
- Spring Security의 커스텀 필터 체인(`JWTCheckFilter`, `CustomLoginSuccessHandler` 등)을 통해 **인가 흐름의 단순화** 및 **예외 처리 일관화**를 달성했습니다.

#### 프로젝트 랭킹 API (Redis + Redisson)
- Redis를 이용해 **프로젝트 랭킹 스냅샷**을 캐싱하여 조회 속도를 향상시켰습니다.
- TTL 만료 시 자동 삭제되며, 캐시 미스 시 DB fallback 로직이 동작합니다.
- 동시 스냅샷 생성 요청 시 중복 연산을 방지하고 **데이터 정합성**을 보장하기 위해 **Redisson 분산 락(RLock)** 을 적용했습니다.  
  - [관련 정리 글](https://juintination.tistory.com/entry/Redis-%EB%8F%99%EC%8B%9C%EC%84%B1-%EC%B2%98%EB%A6%ACLettuce-vs-Redisson)
- k6 및 동시성 테스트를 통해 캐시 적용 전후 **p99 응답 시간 32.3% 개선, 평균 응답 시간 41.5% 감소** 을 검증했습니다.

#### AI 기능 통합
- **FastAPI 기반 AI 서버**와 비동기 통신을 통해 다음과 같은 기능을 제공합니다.
  - **AI 댓글 생성** – 프로젝트 후기를 자동 생성합니다.
  - **AI 뱃지 이미지 생성** – 팀별 프로젝트 데이터를 기반으로 이미지 생성합니다.
  - **AI 챗봇 스트리밍 (SSE)** – Spring WebFlux와 FastAPI SSE 서버를 연동해  
    프로젝트별 AI 챗봇 대화를 **실시간 스트리밍**으로 제공합니다.

#### Kafka 기반 주간 리포트 메일 발송
- 매주 팀의 품앗이 활동 데이터를 집계하여 Kafka를 이용해 **주간 품앗이 리포트 메일**을 비동기로 발송합니다.
  - 리포트에는 AI가 생성한 그래프 이미지, 받은/준 품앗이 수, 팀별 랭킹 변화 등의 시각화 정보가 포함됩니다.
- DLQ(Dead Letter Queue) 구조를 적용하여 실패 메시지를 별도로 관리하고, `x-retry-count`, `x-error-code`, `x-retry-stage` 헤더를 활용해 **무한 루프 없는 재시도**를 구현했습니다.

#### 테스트 및 검증
- MockMvc 및 통합 테스트 환경에서 각 서비스 계층의 안정성을 검증했습니다.
- `CountDownLatch`, `ExecutorService`를 활용해 **동시성 테스트**를 수행했습니다.
- Redis 적용 전후 성능을 k6 부하 테스트로 비교했습니다.

### 사용한 기술 스택

- **Language**
  - Java 21  
- **Framework**
  - Spring Boot 3.4.3  
  - Spring WebFlux (for SSE streaming)  
- **Security**
  - Spring Security  
  - OAuth2 Client (Kakao Provider)  
  - JWT (RTR + HttpOnly Cookie)  
- **Database**
  - MySQL  
  - Redis (Cache)  
  - H2 (for testing)  
- **Concurrency**
  - Redisson RLock  
- **Messaging**
  - Kafka (Producer / Consumer / DLQ)  
- **ORM**
  - JPA + QueryDSL  
- **Monitoring**
  - Prometheus, Grafana  
- **Testing**
  - JUnit5, MockMvc, k6 (부하 테스트)  

### 주요 개선 및 회고

서비스의 **확장성과 안정성**, 그리고 **유지보수성을 높이기 위해 전반적인 코드 구조와 시스템 설계를 개선했습니다.**

주요 개선 내용은 다음과 같습니다.

- **OAuth2 기반 Stateless 인증 구조 확립**
  - 인증의 모든 과정을 **백엔드 단일 책임 구조**로 설계하여,  
    OAuth 코드 교환부터 사용자 검증, JWT 발급까지 전 과정을 서버에서 처리했습니다.  
  - JWT + RTR(Rotate Refresh Token) 구조를 적용하고 Refresh Token을 **HttpOnly 쿠키**에 저장하여  
    세션 없이도 인증 연속성과 보안성을 동시에 확보했습니다.

- **Redis + Redisson 기반 랭킹 캐싱 및 동시성 제어**
  - 프로젝트 랭킹 조회 시 반복되는 DB 접근 부하를 줄이기 위해 Redis 캐시를 도입했습니다.  
  - 동시에 다수의 랭킹 스냅샷 생성 요청이 발생하는 상황을 고려해  
    **Redisson의 RLock(분산 락)** 으로 중복 연산을 방지하고 데이터 정합성을 보장했습니다.  
  - 캐싱 적용 후 **p99 응답 시간 32.3% 개선, 평균 응답 시간 41.5% 감소**를 부하 테스트를 통해 검증했습니다.

- **QueryDSL + Fetch Join을 통한 성능 최적화**
  - 복잡한 조인 쿼리를 **QueryDSL**로 타입 안전하게 작성하고,  
    연관 엔티티 로딩 시 **Fetch Join**을 적용하여 N+1 문제를 해결했습니다.  
  - 다중 조인 관계에서도 한 번의 쿼리로 필요한 데이터를 효율적으로 조회하도록 개선했습니다.

- **공통 유틸리티 기반 코드 구조 개선**
  - 도메인별로 중복 구현되던 페이지네이션 로직을 **공통 유틸리티로 추상화**했습니다.  
  - Builder 패턴 유사 인터페이스를 도입하여 선언적이고 가독성 높은 구조로 개선,  
    다양한 도메인(프로젝트·댓글·뱃지 등)에서 **재사용 가능한 페이지네이션 모듈**을 구현했습니다.  
  - 그 결과 코드 중복이 제거되고 유지보수 비용이 크게 감소했습니다.

- **Kafka 기반 비동기 메일 파이프라인 구축**
  - 주간 품앗이 리포트를 Kafka 기반으로 비동기 처리하여  
    트래픽 피크 타임에도 안정적인 메일 발송이 가능하도록 분리했습니다.  
  - DLQ(Dead Letter Queue) 및 재시도 로직을 추가하여  
    메시지 유실 없이 신뢰성 있는 이벤트 파이프라인을 완성했습니다.

- **SSE 기반 AI 챗봇 스트리밍**
  - Spring WebFlux와 FastAPI SSE 서버를 연동하여  
    **프로젝트별 AI 챗봇 응답을 실시간으로 스트리밍**했습니다.  
  - 비동기 논블로킹 구조로 전환함으로써 사용자 경험과 서버 처리 효율을 동시에 향상시켰습니다.

또한, **서비스의 각 기능을 모듈화하고 계층 간 의존성을 명확히 분리함으로써**,  
새로운 기능을 추가하거나 로직을 확장할 때에도 **안정성과 일관성을 유지할 수 있는 구조**를 완성했습니다.

다만, 기능이 늘어날수록 **헥사고날 아키텍처 등 클린 아키텍처의 필요성**을 강하게 체감했지만,  
시간 제약으로 실제 적용까지는 진행하지 못했습니다.  

이번 프로젝트를 통해 **도메인 중심의 구조적 설계와 서비스 간 책임 분리의 중요성**을 깊이 느꼈으며,  
앞으로는 **확장성과 유지보수성을 동시에 만족시키는 클린 아키텍처 기반의 시스템**을 구축해 나가고자 합니다.
