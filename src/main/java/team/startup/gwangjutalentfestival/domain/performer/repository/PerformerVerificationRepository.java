package team.startup.gwangjutalentfestival.domain.performer.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team.startup.gwangjutalentfestival.domain.performer.entity.PerformerVerificationEntity;

import java.util.Optional;

public interface PerformerVerificationRepository extends JpaRepository<PerformerVerificationEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM PerformerVerificationEntity v WHERE v.codeHash = :codeHash")
    Optional<PerformerVerificationEntity> findByCodeHashForUpdate(@Param("codeHash") String codeHash);
}
