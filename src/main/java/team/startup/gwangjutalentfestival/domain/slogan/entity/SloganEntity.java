package team.startup.gwangjutalentfestival.domain.slogan.entity;

import jakarta.persistence.*;
import lombok.*;
import team.startup.gwangjutalentfestival.domain.slogan.enums.SheetSyncStatus;
import team.startup.gwangjutalentfestival.global.constant.TimeConstants;

import java.time.LocalDateTime;

/**
 * 슬로건 제출 데이터를 저장하는 엔티티.
 * Google Sheets 동기화 상태와 재시도 정보를 함께 관리한다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "slogans")
@Builder
public class SloganEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "slogan", nullable = false, columnDefinition = "TEXT")
    private String slogan;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "school", nullable = false)
    private String school;

    @Column(name = "grade", nullable = false)
    private Integer grade;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "class_num", nullable = false)
    private Integer classNum;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "retry_count")
    private int retryCount;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @Column(columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "synced_at")
    private LocalDateTime syncedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "sheet_sync_status", nullable = false)
    private SheetSyncStatus sheetSyncStatus;

    /**
     * 시트 동기화 상태를 {@link SheetSyncStatus#PROCESSING}으로 변경한다.
     */
    public void processSheetSyncStatus() {
        this.sheetSyncStatus = SheetSyncStatus.PROCESSING;
    }

    /**
     * 시트 동기화 성공 처리를 수행한다.
     * 상태를 {@link SheetSyncStatus#COMPLETED}로 변경하고 동기화 시각을 기록한다.
     */
    public void markDone() {
        this.sheetSyncStatus = SheetSyncStatus.COMPLETED;
        this.syncedAt = LocalDateTime.now(TimeConstants.SEOUL_ZONE_ID);
    }

    /**
     * 시트 동기화 실패 처리를 수행한다.
     * 상태를 {@link SheetSyncStatus#REJECTED}로 변경하고 재시도 예정 시각과 오류 메시지를 기록한다.
     *
     * @param nextRetryAt 다음 재시도 예정 시각
     * @param lastError   마지막 오류 메시지
     */
    public void markFailed(LocalDateTime nextRetryAt, String lastError) {
        this.sheetSyncStatus = SheetSyncStatus.REJECTED;
        this.nextRetryAt = nextRetryAt;
        this.lastError = lastError;
        this.retryCount += 1;
    }

    /**
     * 최대 재시도 횟수 초과로 인해 동기화를 포기 처리한다.
     * 상태를 {@link SheetSyncStatus#EXHAUSTED}로 변경한다.
     */
    public void markExhausted() {
        this.sheetSyncStatus = SheetSyncStatus.EXHAUSTED;
    }
}
