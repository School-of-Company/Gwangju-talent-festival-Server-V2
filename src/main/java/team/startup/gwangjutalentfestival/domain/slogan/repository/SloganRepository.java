package team.startup.gwangjutalentfestival.domain.slogan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.startup.gwangjutalentfestival.domain.slogan.entity.SloganEntity;
import team.startup.gwangjutalentfestival.domain.slogan.enums.SheetSyncStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 슬로건 엔티티에 대한 데이터 접근 인터페이스.
 */
public interface SloganRepository extends JpaRepository<SloganEntity, Long> {

    /**
     * 동일한 전화번호로 제출된 슬로건이 존재하는지 확인한다.
     *
     * @param phoneNumber 확인할 전화번호
     * @return 동일 전화번호 슬로건 존재 여부
     */
    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * 지정된 동기화 상태에 해당하며 다음 재시도 시각이 현재 시각 이전인 슬로건을
     * ID 오름차순으로 최대 50건 조회한다.
     *
     * @param statuses 조회할 동기화 상태 목록
     * @param now      기준 현재 시각
     * @return 처리 대상 슬로건 목록 (최대 50건)
     */
    List<SloganEntity> findTop50BySheetSyncStatusInAndNextRetryAtBeforeOrderByIdAsc(
            List<SheetSyncStatus> statuses,
            LocalDateTime now
    );
}
