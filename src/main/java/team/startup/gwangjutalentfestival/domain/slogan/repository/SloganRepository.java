package team.startup.gwangjutalentfestival.domain.slogan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.startup.gwangjutalentfestival.domain.slogan.entity.SloganEntity;
import team.startup.gwangjutalentfestival.domain.slogan.enums.SheetSyncStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface SloganRepository extends JpaRepository<SloganEntity, Long> {
    boolean existsByPhoneNumber(String phoneNumber);

    List<SloganEntity> findTop50BySheetSyncStatusInAndNextRetryAtBeforeOrderByIdAsc(
            List<SheetSyncStatus> statuses,
            LocalDateTime now
    );}
