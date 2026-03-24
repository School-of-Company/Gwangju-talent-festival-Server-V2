package team.startup.gwangjutalentfestival.domain.team.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import team.startup.gwangjutalentfestival.domain.judge.entity.QJudgementEntity;
import team.startup.gwangjutalentfestival.domain.team.entity.QTeamEntity;
import team.startup.gwangjutalentfestival.domain.team.presentation.data.response.GetTeamRankingResponse;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
@RequiredArgsConstructor
public class TeamRepositoryCustomImpl implements TeamRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QTeamEntity team = QTeamEntity.teamEntity;
    private static final QJudgementEntity judgement = QJudgementEntity.judgementEntity;

    @Override
    public List<GetTeamRankingResponse> getRanking() {
        List<String> teamNames = queryFactory
                .select(team.teamName)
                .from(team)
                .leftJoin(judgement).on(judgement.team.id.eq(team.id))
                .groupBy(team.id, team.teamName)
                .orderBy(
                        team.totalScore.desc(),
                        judgement.expressionCommunicationScore.avg().desc(),
                        judgement.creativityCompositionScore.avg().desc(),
                        judgement.stagePresencePerformanceScore.avg().desc(),
                        team.id.asc()
                )
                .fetch();

        AtomicInteger rank = new AtomicInteger(1);
        return teamNames.stream()
                .map(name -> new GetTeamRankingResponse(
                        rank.getAndIncrement(),
                        name
                ))
                .toList();
    }
}
