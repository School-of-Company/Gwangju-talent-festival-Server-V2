package team.startup.gwangjutalentfestival.domain.slogan.enums;

/**
 * 슬로건의 Google Sheets 동기화 상태를 나타내는 열거형.
 *
 * <ul>
 *   <li>{@link #PENDING} - 동기화 대기 중</li>
 *   <li>{@link #PROCESSING} - 동기화 진행 중</li>
 *   <li>{@link #REJECTED} - 동기화 실패 (재시도 예정)</li>
 *   <li>{@link #COMPLETED} - 동기화 완료</li>
 *   <li>{@link #EXHAUSTED} - 최대 재시도 횟수 초과로 동기화 포기</li>
 * </ul>
 */
public enum SheetSyncStatus {
    PENDING,
    PROCESSING,
    REJECTED,
    COMPLETED,
    EXHAUSTED
}
