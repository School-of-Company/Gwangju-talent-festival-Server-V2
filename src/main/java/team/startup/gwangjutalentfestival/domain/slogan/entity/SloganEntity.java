package team.startup.gwangjutalentfestival.domain.slogan.entity;

import jakarta.persistence.*;
import lombok.*;
import team.startup.gwangjutalentfestival.domain.slogan.enums.SheetSyncStatus;

import java.time.LocalDateTime;

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

    public void processSheetSyncStatus() {
        this.sheetSyncStatus = SheetSyncStatus.PROCESSING;
    }

    public void markDone() {
        this.sheetSyncStatus = SheetSyncStatus.COMPLETED;
        this.syncedAt = LocalDateTime.now();
    }

    public void markFailed(LocalDateTime nextRetryAt, String lastError) {
        this.sheetSyncStatus = SheetSyncStatus.REJECTED;
        this.nextRetryAt = nextRetryAt;
        this.lastError = lastError;
        this.retryCount += 1;
    }

    public void markExhausted() {
        this.sheetSyncStatus = SheetSyncStatus.EXHAUSTED;
    }
}
