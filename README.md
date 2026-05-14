# StaffNow Backend API

> BTL/현장 인력 구인구직 플랫폼 백엔드 서버

## 기술 스택

- **Language**: Java 17
- **Framework**: Spring Boot 3.x
- **Database**: MySQL 8.x
- **ORM**: Spring Data JPA (Hibernate)
- **Authentication**: JWT (Access Token + Refresh Token)
- **API 문서**: Swagger UI
- **Build Tool**: Gradle

---

## 실행 방법

### 1. 사전 준비
- Java 17 이상 설치
- MySQL 8.x 설치 및 실행
- MySQL에 `my_service` 데이터베이스 생성

```sql
CREATE DATABASE my_service;
```

### 2. 환경 설정
`src/main/resources/application.yml` 파일 생성 후 아래 내용 작성:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/my_service?useSSL=false&serverTimezone=Asia/Seoul&allowPublicKeyRetrieval=true
    username: {DB 유저명}
    password: {DB 비밀번호}
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
  servlet:
    multipart:
      enabled: true
      max-file-size: 10MB
      max-request-size: 10MB

file:
  upload-dir: uploads/attendance

jwt:
  secret: {JWT 시크릿 키}
  expiration: 3600000

server:
  port: 8080
```

### 3. 실행
```bash
./gradlew bootRun
```

### 4. Swagger UI
http://localhost:8080/swagger-ui/index.html
---

## API 목록

### 인증 API `/auth`
| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | /auth/login | 로그인 | ❌ |
| POST | /auth/refresh | Access Token 재발급 | ❌ |

### 회원 API `/users`
| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | /users | 회원가입 | ❌ |

### 공고 API `/job-posts`
| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| GET | /job-posts | 전체 공고 조회 (기업용) | ✅ |
| GET | /job-posts/search | 구직자용 공고 검색/필터/정렬 | ✅ |
| GET | /job-posts/my | 내 공고 조회 | ✅ |
| GET | /job-posts/{id} | 공고 단건 조회 | ✅ |
| POST | /job-posts | 공고 생성 (COMPANY) | ✅ |
| PUT | /job-posts/{id} | 공고 수정 (COMPANY) | ✅ |
| DELETE | /job-posts/{id} | 공고 삭제 (COMPANY) | ✅ |
| PATCH | /job-posts/{id}/status | 공고 상태 변경 | ✅ |

### 북마크 API `/job-posts`
| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | /job-posts/{id}/bookmark | 북마크 추가 | ✅ |
| DELETE | /job-posts/{id}/bookmark | 북마크 취소 | ✅ |
| GET | /job-posts/bookmarks | 내 북마크 목록 | ✅ |

### 지원 API `/applications`
| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | /applications/{jobPostId} | 공고 지원 | ✅ |
| DELETE | /applications/{applicationId} | 지원 취소 | ✅ |
| GET | /applications/my | 내 지원 목록 | ✅ |
| GET | /applications/job-posts/{jobPostId} | 공고별 지원자 목록 | ✅ |
| GET | /applications/{applicationId} | 지원자 상세 프로필 | ✅ |
| PATCH | /applications/{applicationId}/approve | 지원 승인 | ✅ |
| PATCH | /applications/{applicationId}/reject | 지원 거절 | ✅ |
| PATCH | /applications/{applicationId}/complete | 근무 완료 처리 | ✅ |
| PATCH | /applications/{applicationId}/no-show | 노쇼 처리 | ✅ |

### 근무회차 API `/job-posts`
| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | /job-posts/{jobPostId}/work-sessions | 근무회차 생성 | ✅ |
| GET | /job-posts/{jobPostId}/work-sessions | 근무회차 조회 | ✅ |
| PATCH | /job-posts/{jobPostId}/work-sessions/{id}/status | 근무회차 상태 변경 | ✅ |

### 계약서 API `/contracts`
| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | /contracts | 계약서 생성 (COMPANY) | ✅ |
| GET | /contracts | 내 계약서 목록 | ✅ |
| GET | /contracts/{contractId} | 계약서 단건 조회 | ✅ |
| PATCH | /contracts/{contractId}/sign | 계약서 서명 | ✅ |
| PATCH | /contracts/{contractId}/cancel | 계약서 취소 (COMPANY) | ✅ |

### 출퇴근 API `/attendance`
| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | /attendance/photo | 출퇴근 사진 업로드 | ✅ |

---

## Role 구분

| Role | 설명 |
|------|------|
| USER | 구직자 (공고 지원, 북마크, 계약서 서명) |
| COMPANY | 기업 (공고 등록, 지원자 관리, 계약서 생성) |

---

## 공고 검색 파라미터

| 파라미터 | 설명 | 예시 |
|---------|------|------|
| title | 공고명 검색 | ?title=스태프 |
| workLocation | 근무 지역 | ?workLocation=서울 |
| companyName | 회사명 검색 | ?companyName=발로라 |
| category | 카테고리 필터 | ?category=EVENT |
| sort | 정렬 방식 | ?sort=wage |
| page | 페이지 번호 (0부터) | ?page=0 |
| size | 페이지 크기 | ?size=10 |

**sort 옵션:**
- `latest` — 최신순 (기본값)
- `wage` — 급여 높은순
- `deadline` — 마감 임박순
- `popular` — 인기순 (조회수 기반)

**category 옵션:**
- `SHORT_TERM` — 단기
- `LONG_TERM` — 장기
- `WEEKEND` — 주말
- `EVENT` — 행사

---

## 프로젝트 구조
src/main/java/com/example/demo/
├── config/          # Security, CORS, Web 설정
├── controller/      # API 엔드포인트
├── dto/             # Request/Response DTO
├── entity/          # JPA 엔티티
├── exception/       # 전역 에러 핸들러
├── jwt/             # JWT 필터 및 유틸
├── repository/      # JPA Repository
└── service/         # 비즈니스 로직

---

## 주의사항

- `application.yml` 은 `.gitignore` 에 포함되어 있어 별도 생성 필요
- 출퇴근 사진은 로컬 `uploads/attendance/` 폴더에 저장 (추후 S3 교체 예정)
- JWT Access Token 만료 시간: 1시간
