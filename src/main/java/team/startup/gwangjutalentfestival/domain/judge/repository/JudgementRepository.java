package team.startup.gwangjutalentfestival.domain.judge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team.startup.gwangjutalentfestival.domain.judge.entity.JudgementEntity;
import team.startup.gwangjutalentfestival.domain.team.entity.TeamEntity;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;

import java.util.List;
import java.util.Optional;

/**
 * 심사 엔티티에 대한 데이터 접근 인터페이스.
 */
public interface JudgementRepository extends JpaRepository<JudgementEntity, Long> {

    /**
     * 팀과 심사위원으로 심사 엔티티를 조회한다.
     *
     * @param team 대상 팀 엔티티
     * @param user 심사위원 사용자 엔티티
     * @return 해당 팀·심사위원 조합의 심사 엔티티 (없으면 empty)
     */
    Optional<JudgementEntity> findByTeamAndUser(TeamEntity team, UserEntity user);

    /**
     * 특정 팀에 대한 모든 심사위원의 점수 합계를 조회한다.
     *
     * @param team 집계 대상 팀 엔티티
     * @return 팀의 총 심사 점수 합계 (심사 데이터가 없으면 {@code null})
     */
    @Query("""
            SELECT SUM(j.expressionCommunicationScore + j.technicalCompletenessScore + j.creativityCompositionScore + j.stagePresencePerformanceScore + j.teamworkStageHarmonyScore)
            FROM JudgementEntity j
            WHERE j.team = :team""")
    Integer sumTotalScoreByTeam(@Param("team") TeamEntity team);

    /**
     * 특정 심사위원이 입력한 모든 심사 목록을 조회한다.
     *
     * @param user 심사위원 사용자 엔티티
     * @return 해당 심사위원의 심사 목록
     */
    List<JudgementEntity> findAllByUser(UserEntity user);
}
