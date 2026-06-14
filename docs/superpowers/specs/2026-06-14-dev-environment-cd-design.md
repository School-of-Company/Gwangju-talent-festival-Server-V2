# Dev 환경 CD 파이프라인 설계

## 개요

`develop` 브랜치 push 시 별도 VM 서버에 자동 배포되는 개발 환경 CD 파이프라인을 구축한다.
기존 prod 환경(`main` → prod 서버)은 변경하지 않는다.

## 목표

- `develop` push → dev 서버 자동 배포
- prod 환경과 완전 격리 (서버 분리)
- 서브도메인으로 dev API 접근 가능 (`dev.api.도메인.com`)
- 기존 `docker-compose.yml` 수정 없음

## 아키텍처

```
GitHub
├── main 브랜치 push
│   └── cd.yml → [self-hosted, prod] runner → prod VM → api.도메인.com
│
└── develop 브랜치 push
    └── cd-develop.yml → [self-hosted, dev] runner → dev VM → dev.api.도메인.com
```

## 변경 사항

### 1. 인프라 (수동 작업)

| 작업 | 내용 |
|------|------|
| 새 VM 생성 | 기존과 동일한 스펙 권장 |
| Docker 설치 | 새 VM에 Docker + Docker Compose 설치 |
| Runner 등록 | 새 VM에 GitHub self-hosted runner 등록, 레이블: `dev` |
| 기존 Runner 레이블 수정 | 기존 prod runner 레이블에 `prod` 추가 |
| 서브도메인 추가 | Hosting.kr에서 `dev.api.도메인.com` A 레코드 → 새 VM IP |

### 2. GitHub Secrets 추가

| Secret 키 | 설명 |
|-----------|------|
| `APPLICATION_YML_DEV` | dev용 application.yml 내용 |
| `ENV_FILE_DEV` | dev용 .env 내용 |

기존 `APPLICATION_YML`, `ENV_FILE`은 prod용으로 그대로 유지.

### 3. 파일 변경

#### 신규: `.github/workflows/cd-develop.yml`

기존 `cd.yml`과 동일한 구조, 아래 3가지만 변경:
- `on.push.branches`: `develop`
- `runs-on`: `[self-hosted, dev]`
- secrets 참조: `APPLICATION_YML_DEV`, `ENV_FILE_DEV`
- 이미지 태그: `gwangju-festival:dev`

#### 수정: `.github/workflows/cd.yml`

- `runs-on`을 `self-hosted` → `[self-hosted, prod]`로 변경 (runner 충돌 방지)

#### 변경 없음

- `docker-compose.yml` — 수정 불필요
- `ci.yml` — 수정 불필요

## 배포 흐름

```
develop 브랜치 push
→ cd-develop.yml 트리거
→ dev VM(self-hosted, dev)에서 실행
→ application.yml 생성 (APPLICATION_YML_DEV)
→ Gradle 빌드
→ Docker 이미지 빌드 (gwangju-festival:dev)
→ .env 생성 (ENV_FILE_DEV)
→ docker compose down → up
→ Discord 알림
```

## 주의 사항

- 기존 prod runner 레이블에 반드시 `prod`를 추가해야 함 — 하지 않으면 두 CD가 같은 runner에서 실행되어 충돌 발생
- dev VM의 방화벽에서 8080 포트 오픈 필요
- dev용 `APPLICATION_YML_DEV`에 DB, Redis 등 dev 전용 설정 값 사용 권장 (prod DB 연결 금지)
