package team.startup.gwangjutalentfestival.domain.slogan.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.slogan.entity.SloganEntity;
import team.startup.gwangjutalentfestival.domain.slogan.enums.SheetSyncStatus;
import team.startup.gwangjutalentfestival.domain.slogan.presentation.data.SloganSheetRowData;
import team.startup.gwangjutalentfestival.domain.slogan.repository.SloganRepository;
import team.startup.gwangjutalentfestival.global.constant.TimeConstants;
import team.startup.gwangjutalentfestival.global.thirdparty.google.adapter.GoogleSheetsAdapter;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SloganSheetSyncScheduler {

    private static final int MAX_RETRY_COUNT = 10;
    private static final int ERROR_MESSAGE_MAX_LENGTH = 2000;

    private final SloganRepository sloganRepository;
    private final GoogleSheetsAdapter googleSheetsAdapter;

    @Transactional
    @Scheduled(fixedDelay = 10000)
    public void execute() {
        List<SloganEntity> slogans = sloganRepository
                .findTop50BySheetSyncStatusInAndNextRetryAtBeforeOrderByIdAsc(
                        List.of(SheetSyncStatus.PENDING, SheetSyncStatus.REJECTED),
                        LocalDateTime.now(TimeConstants.SEOUL_ZONE_ID));

        if (slogans.isEmpty()) {
            return;
        }

        try {
            slogans.forEach(SloganEntity::processSheetSyncStatus);

            List<SloganSheetRowData> rows = slogans.stream()
                    .map(this::toRowData)
                    .toList();

            googleSheetsAdapter.appendSlogan(rows);
            slogans.forEach(SloganEntity::markDone);
        } catch (Exception e) {
            markFailed(slogans, e);
        }
    }

    private SloganSheetRowData toRowData(SloganEntity s) {
        return new SloganSheetRowData(
                s.getSlogan(),
                s.getDescription(),
                s.getSchool(),
                s.getName(),
                s.getGrade(),
                s.getClassNum(),
                s.getPhoneNumber()
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