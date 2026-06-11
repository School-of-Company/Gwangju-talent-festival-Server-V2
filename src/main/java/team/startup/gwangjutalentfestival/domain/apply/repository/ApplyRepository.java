package team.startup.gwangjutalentfestival.domain.apply.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import team.startup.gwangjutalentfestival.domain.apply.entity.ApplyEntity;

/**
 * 공연 신청 엔티티에 대한 데이터 접근 레이어.
 */
@Repository
public interface ApplyRepository extends JpaRepository<ApplyEntity, Long> {
}
