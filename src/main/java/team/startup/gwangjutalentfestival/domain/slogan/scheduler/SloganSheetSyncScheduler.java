package team.startup.gwangjutalentfestival.domain.slogan.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import team.startup.gwangjutalentfestival.domain.slogan.entity.SloganEntity;
import team.startup.gwangjutalentfestival.domain.slogan.enums.SchoolStatus;
import team.startup.gwangjutalentfestival.domain.slogan.enums.SheetSyncStatus;
import team.startup.gwangjutalentfestival.domain.slogan.presentation.data.EnrolledSloganSheetRowData;
import team.startup.gwangjutalentfestival.domain.slogan.presentation.data.OutOfSchoolSloganSheetRowData;
import team.startup.gwangjutalentfestival.domain.slogan.repository.SloganRepository;
import team.startup.gwangjutalentfestival.global.constant.TimeConstants;
import team.startup.gwangjutalentfestival.global.thirdparty.google.adapter.GoogleSheetsAdapter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 슬로건 데이터를 Google Sheets에 주기적으로 동기화하는 스케줄러.
 * 10초 간격으로 실행되며, 대기 중이거나 재시도 대상인 슬로건을 최대 50건씩 처리한다.
 * 동기화 실패 시 재시도 횟수를 증가시키며, 최대 재시도 횟수 초과 시 포기 처리한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SloganSheetSyncScheduler {

    private static final int MAX_RETRY_COUNT = 10;
    private static final int ERROR_MESSAGE_MAX_LENGTH = 2000;

    private final SloganRepository sloganRepository;
    private final GoogleSheetsAdapter googleSheetsAdapter;
    private final TransactionTemplate transactionTemplate;

    /**
     * 10초 간격으로 실행되어 대기 또는 재시도 대상 슬로건을 Google Sheets에 동기화한다.
     * 처리할 데이터가 없으면 즉시 반환한다.
     */
    @Scheduled(fixedDelay = 10000)
    public void execute() {
        List<SloganEntity> slogans = transactionTemplate.execute(status -> {
            List<SloganEntity> fetched = sloganRepository
                    .findTop50BySheetSyncStatusInAndNextRetryAtBeforeOrderByIdAsc(
                            List.of(SheetSyncStatus.PENDING, SheetSyncStatus.REJECTED),
                            LocalDateTime.now(TimeConstants.SEOUL_ZONE_ID));
            fetched.forEach(SloganEntity::processSheetSyncStatus);
            return fetched;
        });

        if (slogans == null || slogans.isEmpty()) {
            return;
        }

        List<SloganEntity> enrolledSlogans = slogans.stream()
                .filter(s -> s.getSchoolStatus() == SchoolStatus.ENROLLED)
                .toList();

        List<SloganEntity> outOfSchoolSlogans = slogans.stream()
                .filter(s -> s.getSchoolStatus() == SchoolStatus.OUT_OF_SCHOOL)
                .toList();

        if (!enrolledSlogans.isEmpty()) {
            List<EnrolledSloganSheetRowData> enrolledRows = enrolledSlogans.stream()
                    .map(this::toEnrolledRowData)
                    .toList();
            try {
                googleSheetsAdapter.appendEnrolledSlogan(enrolledRows);
                transactionTemplate.executeWithoutResult(status -> {
                    enrolledSlogans.forEach(SloganEntity::markDone);
                    sloganRepository.saveAll(enrolledSlogans);
                });
            } catch (Exception e) {
                transactionTemplate.executeWithoutResult(status -> {
                    markFailed(enrolledSlogans, e);
                    sloganRepository.saveAll(enrolledSlogans);
                });
            }
        }

        if (!outOfSchoolSlogans.isEmpty()) {
            List<OutOfSchoolSloganSheetRowData> outOfSchoolRows = outOfSchoolSlogans.stream()
                    .map(this::toOutOfSchoolRowData)
                    .toList();
            try {
                googleSheetsAdapter.appendOutOfSchoolSlogan(outOfSchoolRows);
                transactionTemplate.executeWithoutResult(status -> {
                    outOfSchoolSlogans.forEach(SloganEntity::markDone);
                    sloganRepository.saveAll(outOfSchoolSlogans);
                });
            } catch (Exception e) {
                transactionTemplate.executeWithoutResult(status -> {
                    markFailed(outOfSchoolSlogans, e);
                    sloganRepository.saveAll(outOfSchoolSlogans);
                });
            }
        }
    }

    private EnrolledSloganSheetRowData toEnrolledRowData(SloganEntity s) {
        return new EnrolledSloganSheetRowData(
                s.getSlogan(),
                s.getDescription(),
                s.getSchool(),
                s.getGrade(),
                s.getClassNum(),
                s.getName(),
                s.getPhoneNumber()
        );
    }

    private OutOfSchoolSloganSheetRowData toOutOfSchoolRowData(SloganEntity s) {
        return new OutOfSchoolSloganSheetRowData(
                s.getSlogan(),
                s.getDescription(),
                s.getName(),
                s.getPhoneNumber(),
                s.getBirthDate()
        );
    }

    private void markFailed(List<SloganEntity> slogans, Exception e) {
        String errorMessage = e.getMessage();
        String truncatedError = (errorMessage == null)
                ? "Unknown error"
                : errorMessage.substring(0, Math.min(errorMessage.length(), ERROR_MESSAGE_MAX_LENGTH));

        LocalDateTime nextRetryAt = LocalDateTime.now(TimeConstants.SEOUL_ZONE_ID).plusSeconds(30);

        for (SloganEntity sloganEntity : slogans) {
            if (sloganEntity.getRetryCount() >= MAX_RETRY_COUNT) {
                log.error("슬로건 시트 동기화 최대 재시도 횟수 초과 - sloganId: {}", sloganEntity.getId());
                sloganEntity.markExhausted();
                continue;
            }
            sloganEntity.markFailed(nextRetryAt, truncatedError);
        }
    }
}
