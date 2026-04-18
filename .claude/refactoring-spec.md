# 리팩토링 구현 스펙

> 작성일: 2026-04-18  
> 인터뷰 기반 확정 항목

---

## 1. BanSeatServiceImpl 버그 수정 (즉시)

### 문제
`BanSeatServiceImpl`에서 이미 금지된 좌석을 다시 금지할 때 잘못된 예외를 던짐.

```java
// ❌ 현재 코드 (잘못된 예외)
throw new SeatAlreadyReservedException();

// ✅ 올바른 코드
throw new SeatAlreadyBannedException();
```

### 파일 위치
`domain/seat/service/admin/impl/BanSeatServiceImpl.java`

### 수정 범위
- 단순 Exception 클래스명 교체 1줄

---

## 2. SSE Emitter Manager Generic 추상화

### 문제
`SeatSseEmitterManager`와 `JudgeSseEmitterManager` 두 클래스가 구조와 로직이 거의 동일.

```
// 현재 (중복)
SeatSseEmitterManager  → Map<String, SseEmitter>
JudgeSseEmitterManager → Map<Long, SseEmitter>

addEmitter(), getEmitter(), getAllEmitters()   // 양쪽 동일 로직
```

### 설계 방향

```java
// global/sse/AbstractSseEmitterManager.java
public abstract class AbstractSseEmitterManager<K> {
    protected final Map<K, SseEmitter> emitters = new ConcurrentHashMap<>();
    private static final long TIMEOUT = 60 * 60 * 1000L;

    public SseEmitter addEmitter(K key) {
        SseEmitter emitter = new SseEmitter(TIMEOUT);
        emitters.put(key, emitter);
        emitter.onTimeout(() -> emitters.remove(key));
        emitter.onCompletion(() -> emitters.remove(key));
        emitter.onError(e -> emitters.remove(key));
        return emitter;
    }

    public Optional<SseEmitter> getEmitter(K key) {
        return Optional.ofNullable(emitters.get(key));
    }

    public Collection<SseEmitter> getAllEmitters() {
        return emitters.values();
    }
}

// global/sse/seat/SeatSseEmitterManager.java
@Component
public class SeatSseEmitterManager extends AbstractSseEmitterManager<String> {}

// global/sse/judge/JudgeSseEmitterManager.java
@Component
public class JudgeSseEmitterManager extends AbstractSseEmitterManager<Long> {}
```

### 파일 작업
| 작업 | 파일 |
|------|------|
| 신규 생성 | `global/sse/AbstractSseEmitterManager.java` |
| 수정 | `global/sse/SeatSseEmitterManager.java` (상속으로 간소화) |
| 수정 | `global/sse/JudgeSseEmitterManager.java` (상속으로 간소화) |

---

## 3. SSE Event Listener Generic 추상화

### 문제
`SeatChangeEventListener`, `JudgementTeamEventListener`, `TeamOrderChangedEventListener` 세 클래스가 동일한 @Async + @TransactionalEventListener 패턴을 반복.

```java
// 세 클래스 모두 동일한 구조
@Async("asyncExecutor")
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void execute(XXXEvent event) {
    for (SseEmitter emitter : xxxManager.getAllEmitters()) {
        try {
            emitter.send(SseEmitter.event().name("EVENT_NAME").data(event));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }
}
```

### 설계 방향

```java
// global/sse/AbstractSseEventListener.java
public abstract class AbstractSseEventListener<E> {
    protected abstract AbstractSseEmitterManager<?> getEmitterManager();
    protected abstract String getEventName();
    protected abstract Object getEventData(E event);

    protected void sendToAll(E event) {
        for (SseEmitter emitter : getEmitterManager().getAllEmitters()) {
            try {
                emitter.send(SseEmitter.event()
                    .name(getEventName())
                    .data(getEventData(event)));
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        }
    }
}

// SeatChangeEventListener.java (간소화 후)
@Component
public class SeatChangeEventListener extends AbstractSseEventListener<SeatChangeEvent> {
    private final SeatSseEmitterManager manager;

    @Override
    protected AbstractSseEmitterManager<?> getEmitterManager() { return manager; }

    @Override
    protected String getEventName() { return "SEAT_CHANGE"; }

    @Override
    protected Object getEventData(SeatChangeEvent event) { return event; }

    @Async("asyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void execute(SeatChangeEvent event) { sendToAll(event); }
}
```

### 파일 작업
| 작업 | 파일 |
|------|------|
| 신규 생성 | `global/sse/AbstractSseEventListener.java` |
| 수정 | `domain/seat/event/SeatChangeEventListener.java` |
| 수정 | `domain/judge/event/JudgementTeamEventListener.java` |
| 수정 | `domain/team/event/TeamOrderChangedEventListener.java` |

---

## 구현 순서

1. **BanSeatServiceImpl 버그 수정** — 1줄 변경, 즉시 적용
2. **AbstractSseEmitterManager 추상화** — 기반 클래스 생성 후 기존 Manager 수정
3. **AbstractSseEventListener 추상화** — 기반 클래스 생성 후 기존 Listener 수정

---

## 보류 항목 (현행 유지)

| 항목 | 이유 |
|------|------|
| UserUtil 정적/인스턴스 혼용 | 현행 유지 요청 |
| SaveJudgementScore TODO 주석 | 의도적으로 남긴 것 |
| CancelSeatReservation 두 서비스 분리 | 현행 유지 요청 |
| JWT TokenPayload 도입 | 현행 유지 요청 |
| SloganEntity 상태 관리 분리 | 현행 유지 요청 |
| SeatUtil 하드코딩 | 현행 유지 요청 |
| SeatReservationPolicy 분리 | 현행 유지 요청 |
| 캐시 allEntries=true 세분화 | 현행 유지 요청 |
