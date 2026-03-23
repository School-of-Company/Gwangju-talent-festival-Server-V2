package team.startup.gwangjutalentfestival.domain.team.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import team.startup.gwangjutalentfestival.domain.team.entity.TeamEntity;
import team.startup.gwangjutalentfestival.domain.team.presentation.data.response.TeamRankingResponse;

import java.util.Collection;
import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<TeamEntity, Long> {
    Collection<TeamEntity> findAllByIdIn(Collection<Long> ids);
    List<TeamEntity> findAllByOrderByPerformOrderAsc();

    @Query(value = "select\n" +
            "    row_number() over (\n" +
            "        order by\n" +
            "            t.total_score desc,\n" +
            "            coalesce(j.avg_completion, 0) desc,\n" +
            "            coalesce(j.avg_creativity, 0) desc,\n" +
            "            coalesce(j.avg_stage, 0) desc,\n" +
            "            t.star desc,\n" +
            "            t.id asc\n" +
            "    ) as ranking,\n" +
            "    t.team_name as team_name\n" +
            "from team t\n" +
            "left join (\n" +
            "    select\n" +
            "        j.team_id,\n" +
            "        avg(j.completion_expression)      as avg_completion,\n" +
            "        avg(j.creativity_composition)     as avg_creativity,\n" +
            "        avg(j.stage_manner_performance)   as avg_stage\n" +
            "    from judgement j\n" +
            "    group by j.team_id\n" +
            ") j on j.team_id = t.id\n" +
            "order by ranking asc", nativeQuery = true)
    List<TeamRankingResponse> getRanking();
}
