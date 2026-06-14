# Dev 환경 CD 파이프라인 구축 구현 플랜

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** `develop` 브랜치 push 시 별도 dev VM에 자동 배포되는 CD 파이프라인을 구축한다.

**Architecture:** 기존 prod 환경(main → prod VM)은 건드리지 않고, GitHub self-hosted runner 레이블(`prod` / `dev`)로 두 서버를 구분한다. `docker-compose.yml`은 수정하지 않으며, `cd-develop.yml` 워크플로우 파일 하나를 추가하고 `cd.yml`의 runner 레이블만 수정한다.

**Tech Stack:** GitHub Actions, Docker, Docker Compose, self-hosted runner, Hosting.kr DNS

---

## 파일 변경 맵

| 작업 | 파일 경로 | 내용 |
|------|-----------|------|
| 수정 | `.github/workflows/cd.yml` | `runs-on` 레이블 `[self-hosted, prod]`으로 변경 |
| 신규 | `.github/workflows/cd-develop.yml` | develop 브랜치 CD 워크플로우 |
| 불변 | `docker-compose.yml` | 변경 없음 |

---

## Task 1: 기존 prod runner에 레이블 추가 (GitHub UI — 수동)

> 이 작업은 코드 변경이 아닌 GitHub 웹 UI 작업이다.  
> **반드시 Task 2보다 먼저 수행해야 한다.** 레이블 없이 `cd.yml`을 먼저 배포하면 runner를 못 찾아 배포가 실패한다.

- [ ] **Step 1: GitHub 레포지토리 → Settings → Actions → Runners 이동**

  브라우저에서 `https://github.com/<org>/<repo>/settings/actions/runners` 접속

- [ ] **Step 2: 기존 self-hosted runner 클릭 → Edit**

  현재 등록된 runner(prod 서버에 설치된 것)를 클릭한다.

- [ ] **Step 3: Labels 필드에 `prod` 추가 후 저장**

  기존에 `self-hosted` 레이블만 있다면 `prod`를 추가한다.  
  저장 후 Labels에 `self-hosted, prod` 두 개가 표시되면 완료.

---

## Task 2: `cd.yml` runner 레이블 수정

**Files:**
- Modify: `.github/workflows/cd.yml:10`

- [ ] **Step 1: `runs-on` 값 수정**

  `.github/workflows/cd.yml` 10번째 줄을 아래와 같이 수정한다.

  **변경 전:**
  ```yaml
  runs-on: self-hosted
  ```

  **변경 후:**
  ```yaml
  runs-on: [self-hosted, prod]
  ```

- [ ] **Step 2: 커밋**

  ```bash
  git add .github/workflows/cd.yml
  git commit -m "update :: prod runner 레이블 명시"
  ```

---

## Task 3: `cd-develop.yml` 워크플로우 신규 생성

**Files:**
- Create: `.github/workflows/cd-develop.yml`

- [ ] **Step 1: 파일 생성**

  `.github/workflows/cd-develop.yml`을 아래 내용으로 생성한다.

  ```yaml
  name: CD (develop)

  on:
    push:
      branches: ["develop"]

  jobs:
    build:
      runs-on: [self-hosted, dev]

      steps:
        - name: Checkout repository
          uses: actions/checkout@v3

        - name: Set up JDK 21
          uses: actions/setup-java@v3
          with:
            distribution: temurin
            java-version: 21
            cache: gradle

        - name: Grant execute permission for gradlew
          run: chmod +x gradlew

        - name: Create application.yml
          run: |
            mkdir -p src/main/resources
            echo "${{ secrets.APPLICATION_YML_DEV }}" > src/main/resources/application.yml

        - name: Build with Gradle
          run: ./gradlew build -x test --no-daemon

        - name: Build Docker image
          run: docker build -t gwangju-festival:dev .

        - name: Create .env file
          env:
            ENV_FILE: ${{ secrets.ENV_FILE_DEV }}
          run: printf '%s\n' "$ENV_FILE" > .env

        - name: Deploy with Docker Compose
          run: |
            docker compose --env-file .env down --remove-orphans
            docker compose --env-file .env up -d

        - name: Notify Discord on success
          if: success()
          run: |
            curl -H "Content-Type: application/json" \
                 -d "{\"content\": \"✅ [DEV] 배포 성공\\n브랜치: ${{ github.ref_name }}\\n커밋: ${{ github.sha }}\"}" \
                 ${{ secrets.DISCORD_WEBHOOK_URL }}

        - name: Notify Discord on failure
          if: failure()
          run: |
            curl -H "Content-Type: application/json" \
                 -d "{\"content\": \"❌ [DEV] 배포 실패\\n브랜치: ${{ github.ref_name }}\\n커밋: ${{ github.sha }}\"}" \
                 ${{ secrets.DISCORD_WEBHOOK_URL }}
  ```

- [ ] **Step 2: 커밋 (아직 push하지 않는다)**

  ```bash
  git add .github/workflows/cd-develop.yml
  git commit -m "add :: develop 브랜치 CD 워크플로우 추가"
  ```

  > **주의:** 아직 push하면 안 된다. dev 서버 runner가 등록되지 않은 상태에서 push하면 워크플로우가 `queued` 상태로 멈춘다. Task 4~6 완료 후 push한다.

---

## Task 4: dev VM 서버 초기 설정 (서버 SSH 접속 — 수동)

> 새로 생성한 VM에 SSH로 접속하여 아래 명령어를 순서대로 실행한다.

- [ ] **Step 1: Docker 설치**

  ```bash
  sudo apt-get update
  sudo apt-get install -y ca-certificates curl gnupg
  sudo install -m 0755 -d /etc/apt/keyrings
  curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
  sudo chmod a+r /etc/apt/keyrings/docker.gpg
  echo \
    "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
    $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
    sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
  sudo apt-get update
  sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
  ```

- [ ] **Step 2: Docker 권한 설정**

  ```bash
  sudo usermod -aG docker $USER
  newgrp docker
  ```

- [ ] **Step 3: Docker 동작 확인**

  ```bash
  docker --version
  docker compose version
  ```

  기대 출력 예시:
  ```
  Docker version 26.x.x, build xxxxxxx
  Docker Compose version v2.x.x
  ```

- [ ] **Step 4: GitHub self-hosted runner 다운로드 및 등록**

  GitHub 레포지토리 → Settings → Actions → Runners → New self-hosted runner 클릭  
  OS: Linux / Architecture: x64 선택 후 표시되는 명령어를 그대로 실행한다.

  대략적인 형태 (토큰은 GitHub UI에서 발급):
  ```bash
  mkdir actions-runner && cd actions-runner
  curl -o actions-runner-linux-x64-2.x.x.tar.gz -L https://github.com/actions/runner/releases/download/v2.x.x/actions-runner-linux-x64-2.x.x.tar.gz
  tar xzf ./actions-runner-linux-x64-2.x.x.tar.gz
  ./config.sh --url https://github.com/<org>/<repo> --token <GITHUB_TOKEN>
  ```

- [ ] **Step 5: runner 레이블을 `dev`로 지정**

  `./config.sh` 실행 중 아래 프롬프트가 나오면 `dev`를 입력한다:
  ```
  Enter any additional labels (ex. label-1,label-2): dev
  ```

- [ ] **Step 6: runner를 서비스로 등록하여 서버 재시작 후에도 자동 실행**

  ```bash
  sudo ./svc.sh install
  sudo ./svc.sh start
  sudo ./svc.sh status
  ```

  기대 출력:
  ```
  Active: active (running)
  ```

- [ ] **Step 7: GitHub UI에서 runner 등록 확인**

  `https://github.com/<org>/<repo>/settings/actions/runners` 에서  
  새 runner가 `Idle` 상태이고 Labels에 `self-hosted`, `dev`가 표시되면 완료.

- [ ] **Step 8: 8080 포트 방화벽 오픈**

  VM 제공사 콘솔(인바운드 규칙)에서 TCP 8080 포트를 허용한다.  
  또는 ufw 사용 시:
  ```bash
  sudo ufw allow 8080/tcp
  sudo ufw status
  ```

---

## Task 5: Hosting.kr 서브도메인 DNS 설정 (수동)

- [ ] **Step 1: 새 VM의 공인 IP 확인**

  VM 제공사 콘솔 또는 서버에서:
  ```bash
  curl ifconfig.me
  ```

- [ ] **Step 2: Hosting.kr 로그인 → DNS 관리 → A 레코드 추가**

  | 필드 | 값 |
  |------|-----|
  | 유형 | A |
  | 호스트명 | `dev.api` (도메인이 `example.com`이면 → `dev.api.example.com`) |
  | 값(IP) | 새 VM 공인 IP |
  | TTL | 300 (기본값) |

- [ ] **Step 3: DNS 전파 확인 (최대 10분 소요)**

  로컬 터미널에서:
  ```bash
  nslookup dev.api.도메인.com
  ```

  기대 출력:
  ```
  Address: <새 VM 공인 IP>
  ```

---

## Task 6: GitHub Secrets 추가 (GitHub UI — 수동)

> `https://github.com/<org>/<repo>/settings/secrets/actions` 접속

- [ ] **Step 1: `APPLICATION_YML_DEV` Secret 추가**

  Name: `APPLICATION_YML_DEV`  
  Value: dev 서버용 `application.yml` 전체 내용  
  (DB URL, Redis host 등을 dev 전용 값으로 변경. **절대 prod DB에 연결하지 말 것**)

- [ ] **Step 2: `ENV_FILE_DEV` Secret 추가**

  Name: `ENV_FILE_DEV`  
  Value: dev 서버용 `.env` 전체 내용  
  (prod `.env`를 복사한 뒤 dev 환경에 맞게 값 수정)

  > `DISCORD_WEBHOOK_URL`은 기존 Secret을 그대로 공유하므로 추가 불필요.

---

## Task 7: 코드 push 및 배포 검증

- [ ] **Step 1: Task 2~3에서 만든 커밋을 develop 브랜치에 push**

  ```bash
  git push origin develop
  ```

- [ ] **Step 2: GitHub Actions 탭에서 `CD (develop)` 워크플로우 실행 확인**

  `https://github.com/<org>/<repo>/actions` 에서  
  `CD (develop)` 워크플로우가 `In progress` → `Success`로 전환되는지 확인.

- [ ] **Step 3: dev 서버에서 컨테이너 실행 확인**

  dev VM SSH 접속 후:
  ```bash
  docker ps
  ```

  기대 출력 (4개 컨테이너 모두 `Up` 상태):
  ```
  CONTAINER ID   IMAGE                     STATUS
  xxxxxxxxxxxx   gwangju-festival:dev      Up N seconds
  xxxxxxxxxxxx   redis:7.0                 Up N seconds
  xxxxxxxxxxxx   prom/prometheus:v2.51.2   Up N seconds
  xxxxxxxxxxxx   grafana/grafana:10.4.2    Up N seconds
  ```

- [ ] **Step 4: 서브도메인으로 API 응답 확인**

  ```bash
  curl -i http://dev.api.도메인.com:8080/actuator/health
  ```

  기대 응답:
  ```json
  {"status":"UP"}
  ```

- [ ] **Step 5: Discord 알림 확인**

  Discord 채널에 `✅ [DEV] 배포 성공` 메시지가 도착했는지 확인.

- [ ] **Step 6: main 브랜치 push로 prod 배포 영향 없음 확인**

  prod 서버에서:
  ```bash
  docker ps --filter "name=spring-app"
  ```

  prod 컨테이너가 `gwangju-festival:latest` 이미지 그대로 실행 중인지 확인.  
  dev 배포로 인해 prod 컨테이너가 재시작되거나 중단되면 안 된다.
