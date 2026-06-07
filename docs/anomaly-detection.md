# 이상 탐지 모듈 (Anomaly Detection)

## 개요

rule-based detector 방식으로 구현된 이상 탐지 모듈입니다. ML 기반이 아닌, 사전 정의된 규칙(임계값)에 따라 Prometheus 메트릭을 주기적으로 조회하고 이상을 감지합니다.

감지 주기: 60초 (fixedDelay)

## 환경 변수

| 변수명 | 기본값 | 설명 |
|--------|--------|------|
| `ANOMALY_DETECTOR_ENABLED` | `false` | 이상 탐지 스케줄러 활성화 여부 |
| `PROMETHEUS_BASE_URL` | `http://localhost:9090` | Prometheus 서버 주소 |
| `DISCORD_WEBHOOK_URL` | (없음) | Discord 알림 웹훅 URL (빈값이면 알림 비활성화) |

## 탐지 규칙

| 도메인 | 메트릭 | 임계값 | 설명 |
|--------|--------|--------|------|
| seat | failure_rate | 5% | 좌석 예매 실패율 |
| seat | p95_duration | 2.5s | 좌석 예매 p95 응답 시간 |
| judge | failure_rate | 3% | 심사 제출 실패율 |
| judge | p95_duration | 2.0s | 심사 제출 p95 응답 시간 |

## Prometheus Query API

Prometheus의 instant query 엔드포인트를 사용합니다.

```
GET {PROMETHEUS_BASE_URL}/api/v1/query?query={promql}
```

### 실패율 PromQL 패턴

```
rate(seat_reservation_failure_total{application="gwangjutalentfestival"}[5m])
/ (
  rate(seat_reservation_success_total{application="gwangjutalentfestival"}[5m])
  + rate(seat_reservation_failure_total{application="gwangjutalentfestival"}[5m])
)
```

### p95 응답시간 PromQL 패턴

```
histogram_quantile(0.95, sum by (le) (rate(seat_reservation_duration_seconds_bucket{application="gwangjutalentfestival"}[5m])))
```

## Discord 알림 예시

```
🚫 [seat] failure_rate 이상: 7.20% >= 5.00%
(좌석 예매 실패율이 기준치를 초과했습니다.)
```

```
🚫 [judge] p95_duration 이상: 3.10s >= 2.00s
(심사 제출 p95 응답 시간이 기준치를 초과했습니다.)
```

동일 도메인/메트릭에 대한 OPEN 이벤트가 이미 존재하면 중복 알림을 보내지 않습니다.

## 실행 방법

### 활성화

`ANOMALY_DETECTOR_ENABLED=true` 환경 변수를 설정합니다.

```bash
ANOMALY_DETECTOR_ENABLED=true \
PROMETHEUS_BASE_URL=http://prometheus:9090 \
DISCORD_WEBHOOK_URL=https://discord.com/api/webhooks/... \
java -jar app.jar
```

### curl 테스트

Prometheus instant query API 직접 호출:

```bash
# 좌석 예매 실패율 조회
curl -G 'http://localhost:9090/api/v1/query' \
  --data-urlencode 'query=rate(seat_reservation_failure_total{application="gwangjutalentfestival"}[5m]) / (rate(seat_reservation_success_total{application="gwangjutalentfestival"}[5m]) + rate(seat_reservation_failure_total{application="gwangjutalentfestival"}[5m]))'

# 좌석 예매 p95 응답시간 조회
curl -G 'http://localhost:9090/api/v1/query' \
  --data-urlencode 'query=histogram_quantile(0.95, sum by (le) (rate(seat_reservation_duration_seconds_bucket{application="gwangjutalentfestival"}[5m])))'
```

Discord 웹훅 직접 테스트:

```bash
curl -X POST https://discord.com/api/webhooks/YOUR_WEBHOOK_URL \
  -H "Content-Type: application/json" \
  -d '{"content": "🚫 [test] anomaly detection 테스트 메시지"}'
```

## Feedback Loop

anomaly_event에 대한 운영자 피드백을 등록하고 이벤트 상태를 관리하는 기능입니다.

### 상태 전이 규칙

| FeedbackLabel | anomaly_event 상태 전이 | resolved_at |
|---------------|------------------------|-------------|
| `TRUE_INCIDENT` | OPEN → **RESOLVED** | 피드백 등록 시점 |
| `FALSE_POSITIVE` | OPEN → **RESOLVED** | 피드백 등록 시점 |
| `IGNORED` | OPEN → **IGNORED** | 피드백 등록 시점 |

### falsePositiveRate 정의

```
falsePositiveRate = falsePositiveCount / (trueIncidentCount + falsePositiveCount)
```

- OPEN 또는 IGNORED 상태 이벤트(피드백 없음)는 분모에서 제외됩니다.
- 분모가 0이면 falsePositiveRate는 0.0입니다.

### API 목록

모든 API는 ADMIN 권한이 필요합니다. `Authorization: Bearer {token}` 헤더를 포함해야 합니다.

#### 이상 탐지 이벤트 목록 조회

```bash
# 전체 목록 (최신순, 기본 20개)
curl -H "Authorization: Bearer TOKEN" \
  'http://localhost:8080/monitoring/anomalies?page=0&size=20'

# OPEN 상태 필터
curl -H "Authorization: Bearer TOKEN" \
  'http://localhost:8080/monitoring/anomalies?page=0&size=20&status=OPEN'
```

#### 이상 탐지 이벤트 단일 조회

```bash
curl -H "Authorization: Bearer TOKEN" \
  http://localhost:8080/monitoring/anomalies/1
```

#### 피드백 등록

```bash
# 실제 장애로 판단
curl -X POST http://localhost:8080/monitoring/anomalies/1/feedback \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"label": "TRUE_INCIDENT", "note": "실제 트래픽 급증으로 인한 장애"}'

# 오탐으로 판단
curl -X POST http://localhost:8080/monitoring/anomalies/1/feedback \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"label": "FALSE_POSITIVE", "note": "정상적인 이벤트 트래픽"}'

# 무시
curl -X POST http://localhost:8080/monitoring/anomalies/1/feedback \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"label": "IGNORED"}'
```

#### 요약 조회

```bash
curl -H "Authorization: Bearer TOKEN" \
  http://localhost:8080/monitoring/anomalies/summary
```

응답 예시:
```json
{
  "totalEvents": 10,
  "openEvents": 3,
  "resolvedEvents": 5,
  "ignoredEvents": 2,
  "trueIncidentCount": 3,
  "falsePositiveCount": 2,
  "falsePositiveRate": 0.4
}
```

## ML Server 연동

rule-based 탐지가 이상을 감지한 경우, 보조 신호로 ML Server의 anomaly score를 함께 요청합니다.

### 역할 분리

- Rule-based threshold 초과가 `anomaly_event` 생성 트리거입니다.
- ML `predictedLabel`은 이벤트 생성 여부를 결정하지 않습니다.
- ML이 `normal`을 반환하더라도 rule threshold를 초과했다면 `anomaly_event`는 저장됩니다.
- `anomalyScore`, `predictedLabel`, `modelVersion`은 운영자 판단을 돕는 보조 정보로만 저장됩니다.

### 환경 변수

| 변수명 | 기본값 | 설명 |
|--------|--------|------|
| `ML_ANOMALY_SCORE_ENABLED` | `false` | ML Server 호출 활성화 여부 |
| `ML_SERVER_BASE_URL` | (없음) | ML Server 주소 (예: `http://<ml-server-host>:<port>`) |
| `ML_SERVER_TIMEOUT_MS` | `3000` | ML Server read timeout (ms) |

실제 ML Server host / port는 환경 변수로만 주입하고, 코드나 문서에 직접 기재하지 않습니다.

### 요청 스키마

ML Server 호출 URL: `POST {ML_SERVER_BASE_URL}/anomaly-score`

| 필드 | 타입 | 설명 |
|------|------|------|
| `domain` | String | `SEAT` 또는 `JUDGE` (대문자) |
| `metricName` | String | `failure_rate` 또는 `p95_duration` |
| `value` | double | Prometheus에서 조회한 실제 관측값 |
| `hourOfDay` | int | 탐지 시점 기준 0~23 |
| `dayOfWeek` | int | 탐지 시점 기준 1(월)~7(일) |

### Fallback 정책

ML Server 호출이 실패하거나 비활성화된 경우에도 Spring 이상 탐지는 계속 동작합니다.

- `enabled=false` 또는 `baseUrl`이 비어 있으면 ML 호출 자체를 건너뜁니다.
- HTTP 오류, timeout, `modelLoaded=false`, `anomalyScore=null`, 유효하지 않은 `predictedLabel`은 모두 ML 결과 없음으로 처리합니다.
- ML 결과가 없으면 `anomalyScore`, `modelVersion`, `predictedLabel`은 `null`로 저장됩니다.
- ML 결과가 없어도 rule-based `anomaly_event` 저장과 Discord 알림은 정상 진행됩니다.

### Discord 알림 예시

ML 결과 있음:
```
🚫 [seat] failure_rate 이상: 7.20% >= 5.00%
(좌석 예매 실패율이 기준치를 초과했습니다.)
ML Score: 0.0794 (anomaly)
Model Version: iforest-v1
```

ML 결과 없음:
```
🚫 [seat] failure_rate 이상: 7.20% >= 5.00%
(좌석 예매 실패율이 기준치를 초과했습니다.)
ML: Rule-based only
```

## Dataset Export

anomaly_event + incident_feedback 데이터를 Prometheus 시계열과 결합하여 ML 학습용 CSV 데이터셋을 생성합니다.
Isolation Forest 등 이상 탐지 모델 학습에 사용합니다. 학습 자체는 이 서버의 범위가 아닙니다.

### 환경 변수

| 변수명 | 기본값 | 설명 |
|--------|--------|------|
| `MONITORING_DATASET_EXPORT_PATH` | `./exports` | CSV 파일 저장 경로 |

### API

```bash
# 전체 기간 export (7일 이내, step=60초)
curl -X POST http://localhost:8080/monitoring/datasets/export \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "start": "2026-05-25T00:00:00",
    "end": "2026-06-01T00:00:00",
    "stepSeconds": 60
  }'

# seat 도메인만 export
curl -X POST http://localhost:8080/monitoring/datasets/export \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "start": "2026-05-25T00:00:00",
    "end": "2026-06-01T00:00:00",
    "stepSeconds": 60,
    "domain": "seat"
  }'

# 특정 메트릭만 export
curl -X POST http://localhost:8080/monitoring/datasets/export \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "start": "2026-05-25T00:00:00",
    "end": "2026-06-01T00:00:00",
    "domain": "seat",
    "metricName": "failure_rate"
  }'
```

응답 예시:
```json
{
  "filePath": "./exports/dataset_20260601_143205.csv",
  "rowCount": 10080
}
```

### 요청 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| start | ISO-8601 | ✓ | 조회 시작 시각 (Asia/Seoul 기준) |
| end | ISO-8601 | ✓ | 조회 종료 시각, start보다 이후여야 함 |
| stepSeconds | Integer | - | step 간격(초), 60~3600, 기본값 60 |
| domain | String | - | seat 또는 judge, null이면 전체 |
| metricName | String | - | failure_rate 또는 p95_duration, null이면 전체 |

### CSV 스키마

```csv
domain,metricName,timestamp,value,hourOfDay,dayOfWeek,label
seat,failure_rate,2026-06-01T09:00:00,0.023,9,MONDAY,normal
seat,failure_rate,2026-06-01T09:01:00,0.087,9,MONDAY,anomaly
seat,p95_duration,2026-06-01T09:00:00,1.234,9,MONDAY,normal
```

| 필드 | 타입 | 설명 |
|------|------|------|
| domain | String | seat / judge |
| metricName | String | failure_rate / p95_duration |
| timestamp | ISO-8601 | step 단위 정규화, Asia/Seoul 기준 |
| value | double | Prometheus 관측값 |
| hourOfDay | int | 0~23 (Asia/Seoul 기준) |
| dayOfWeek | String | MONDAY~SUNDAY |
| label | String | anomaly / normal |

### label 매핑 규칙

| 조건 | label | CSV 포함 |
|------|-------|--------|
| TRUE_INCIDENT feedback 있는 포인트 | anomaly | ✓ |
| FALSE_POSITIVE feedback 있는 포인트 | normal | ✓ |
| anomaly_event가 없는 일반 시계열 포인트 | normal | ✓ |
| IGNORED feedback 있는 포인트 | — | ✗ 제외 |
| feedback 없는 OPEN anomaly_event 포인트 | — | ✗ 제외 |

### label 정책 한계

- 일반 시계열 포인트는 별도 incident feedback이 없는 구간이므로 normal로 간주합니다. 실제 탐지되지 않은 이상 징후가 포함될 수 있습니다.
- TRUE_INCIDENT는 createdAt이 속한 step bucket 1개만 anomaly로 라벨링합니다. 이상 지속 구간 전체를 커버하지 않습니다.
- IGNORED와 feedback 없는 OPEN 이벤트는 CSV에서 제외됩니다. 학습 데이터 양이 줄어들 수 있습니다.
- 동일 timestamp에 Prometheus series가 여러 개 반환될 경우 첫 번째 정상 값을 사용합니다. (중복 row 방지)
