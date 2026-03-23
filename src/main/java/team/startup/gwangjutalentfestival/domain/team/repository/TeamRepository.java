package team.startup.gwangjutalentfestival.domain.team.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import team.startup.gwangjutalentfestival.domain.team.entity.TeamEntity;

import java.util.Collection;
import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<TeamEntity, Long>, TeamRepositoryCustom {
    Collection<TeamEntity> findAllByIdIn(Collection<Long> ids);
    List<TeamEntity> findAllByOrderByPerformOrderAsc();
}
