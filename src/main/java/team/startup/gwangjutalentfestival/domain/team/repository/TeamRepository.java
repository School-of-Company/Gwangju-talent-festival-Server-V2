package team.startup.gwangjutalentfestival.domain.team.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import team.startup.gwangjutalentfestival.domain.team.entity.TeamEntity;

import java.util.Collection;
import java.util.List;

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
}
