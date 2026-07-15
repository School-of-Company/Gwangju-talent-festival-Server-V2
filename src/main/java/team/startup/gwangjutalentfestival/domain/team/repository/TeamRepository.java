package team.startup.gwangjutalentfestival.domain.team.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import team.startup.gwangjutalentfestival.domain.team.entity.TeamEntity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 팀 엔티티에 대한 데이터 접근 레이어.
 */
@Repository
public interface TeamRepository extends JpaRepository<TeamEntity, Long>, TeamRepositoryCustom {

    /**
     * 주어진 ID 목록에 해당하는 팀들을 조회한다.
     *
     * @param ids 조회할 팀 ID 컬렉션
     * @return 해당 ID를 가진 팀 컬렉션
     */
    Collection<TeamEntity> findAllByIdIn(Collection<Long> ids);

    /**
     * 모든 팀을 공연 순서 오름차순으로 조회한다.
     *
     * @return 공연 순서로 정렬된 팀 목록
     */
    List<TeamEntity> findAllByOrderByPerformOrderAsc();

    /**
     * 비관적 락(PESSIMISTIC_WRITE)을 걸어 팀을 조회한다.
     * 여러 심사위원이 동시에 같은 팀의 총점을 갱신할 때 갱신 분실(Lost Update)을 방지하기 위해 사용한다.
     *
     * @param id 조회할 팀 ID
     * @return 락이 걸린 팀 엔티티 (없으면 empty)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TeamEntity t WHERE t.id = :id")
    Optional<TeamEntity> findByIdForUpdate(@Param("id") Long id);
}
