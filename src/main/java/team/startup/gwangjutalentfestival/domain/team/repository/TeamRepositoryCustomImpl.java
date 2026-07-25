package team.startup.gwangjutalentfestival.domain.team.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import team.startup.gwangjutalentfestival.domain.judge.entity.JudgementEntity;
import team.startup.gwangjutalentfestival.domain.judge.entity.QJudgementEntity;
import team.startup.gwangjutalentfestival.domain.judge.util.JudgeRankingCalculator;
import team.startup.gwangjutalentfestival.domain.team.entity.TeamEntity;
import team.startup.gwangjutalentfestival.domain.team.entity.QTeamEntity;
import team.startup.gwangjutalentfestival.domain.team.presentation.data.response.GetTeamRankingResponse;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * {@link TeamRepositoryCustom}의 QueryDSL 구현체.
 * 총점 및 세부 심사 점수를 기준으로 팀 랭킹을 계산한다.
 */
@Repository
@RequiredArgsConstructor
public class TeamRepositoryCustomImpl implements TeamRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QTeamEntity team = QTeamEntity.teamEntity;
    private static final QJudgementEntity judgement = QJudgementEntity.judgementEntity;

    /**
     * 총점 내림차순, 세부 심사 점수의 최고·최저 제외 평균 순으로 팀 랭킹을 조회한다.
     */
    @Override
    public List<GetTeamRankingResponse> getRanking() {
        List<TeamEntity> teams = queryFactory.selectFrom(team).fetch();
        List<JudgementEntity> judgements = queryFactory
                .selectFrom(judgement)
                .join(judgement.user).fetchJoin()
                .where(judgement.user.role.eq(Role.JUDGE))
                .fetch();
        Map<Long, Integer> calculatedScores = teams.stream()
                .collect(Collectors.toMap(TeamEntity::getId, TeamEntity::getTotalScore));
        Map<Long, Integer> ranks = JudgeRankingCalculator.calculate(
                teams, judgements, calculatedScores);

        return teams.stream()
                .sorted(Comparator.comparing(team -> ranks.get(team.getId())))
                .map(team -> new GetTeamRankingResponse(
                        ranks.get(team.getId()),
                        team.getTeamName()
                ))
                .toList();
    }
}
