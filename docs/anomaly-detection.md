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
