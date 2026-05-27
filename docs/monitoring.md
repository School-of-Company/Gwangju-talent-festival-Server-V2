# Prometheus 모니터링 쿼리

## 성공 metric 기준

`seat.reservation.success` / `judge.submit.success` 는 서비스 `execute()` 메서드가 **Exception 없이 정상 종료된 시점**을 기준으로 기록됩니다.
트랜잭션 commit 완료 이후의 성공까지 보장하지 않습니다.

---

## HTTP Endpoint

### Request Rate (5분 기준)
```promql
rate(http_server_requests_seconds_count{application="gwangjutalentfestival"}[5m])
```
> endpoint별 초당 요청 수. `uri`, `method`, `status` 레이블로 필터링 가능.

### 5xx Error Rate
```promql
rate(http_server_requests_seconds_count{application="gwangjutalentfestival", status=~"5.."}[5m])
```
> 5xx 응답이 발생하는 endpoint와 비율 확인.

### p95 Latency
```promql
histogram_quantile(0.95, sum by (le, uri, method) (rate(http_server_requests_seconds_bucket{application="gwangjutalentfestival"}[5m])))
```
> endpoint별 p95 응답 시간(초 단위). `uri` 레이블로 특정 endpoint만 필터링 가능.

---

## 좌석 예매 (Seat Reservation)

### 성공률 (5분 기준)
```promql
rate(seat_reservation_success_total[5m])
```
> 초당 좌석 예매 성공 횟수.

### 실패율 (5분 기준)
```promql
rate(seat_reservation_failure_total[5m])
```
> 초당 좌석 예매 실패 횟수. Exception 발생 기준.

### 성공 대비 실패 비율
```promql
rate(seat_reservation_failure_total[5m])
/ (rate(seat_reservation_success_total[5m]) + rate(seat_reservation_failure_total[5m]))
```
> 전체 예매 시도 중 실패 비율 (0~1).

### p95 Duration
```promql
histogram_quantile(0.95, sum by (le) (rate(seat_reservation_duration_seconds_bucket[5m])))
```
> 좌석 예매 `execute()` 전체 소요 시간의 p95 (초 단위).

---

## 심사 제출 (Judge Submit)

### 성공률 (5분 기준)
```promql
rate(judge_submit_success_total[5m])
```
> 초당 심사 제출 성공 횟수.

### 실패율 (5분 기준)
```promql
rate(judge_submit_failure_total[5m])
```
> 초당 심사 제출 실패 횟수. Exception 발생 기준.

### 성공 대비 실패 비율
```promql
rate(judge_submit_failure_total[5m])
/ (rate(judge_submit_success_total[5m]) + rate(judge_submit_failure_total[5m]))
```
> 전체 심사 제출 시도 중 실패 비율 (0~1).

### p95 Duration
```promql
histogram_quantile(0.95, sum by (le) (rate(judge_submit_duration_seconds_bucket{application="gwangjutalentfestival"}[5m])))
```
> 심사 제출 `execute()` 전체 소요 시간의 p95 (초 단위).

---

## 노출 확인

```bash
curl http://localhost:8080/prometheus | grep -E "seat_reservation|judge_submit|http_server_requests"
```
