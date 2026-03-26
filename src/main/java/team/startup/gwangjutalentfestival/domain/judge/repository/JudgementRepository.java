package team.startup.gwangjutalentfestival.domain.judge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team.startup.gwangjutalentfestival.domain.judge.entity.JudgementEntity;
import team.startup.gwangjutalentfestival.domain.team.entity.TeamEntity;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;

import java.util.List;
import java.util.Optional;

public interface JudgementRepository extends JpaRepository<JudgementEntity, Long> {
    Optional<JudgementEntity> findByTeamAndUser(TeamEntity team, UserEntity user);

    @Query("SELECT SUM(" +
            "j.expressionCommunicationScore + " +
            "j.technicalCompletenessScore + " +
            "j.creativityCompositionScore + " +
            "j.stagePresencePerformanceScore + " +
            "j.teamworkStageHarmonyScore) " +
            "FROM JudgementEntity j" +
            " WHERE j.team = :team")
    Integer sumTotalScoreByTeam(@Param("team") TeamEntity team);

    List<JudgementEntity> findAllByUser(UserEntity user);
}
