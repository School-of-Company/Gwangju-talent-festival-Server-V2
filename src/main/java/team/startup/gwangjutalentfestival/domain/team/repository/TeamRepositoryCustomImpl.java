package team.startup.gwangjutalentfestival.domain.team.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import team.startup.gwangjutalentfestival.domain.judge.entity.QJudgementEntity;
import team.startup.gwangjutalentfestival.domain.team.entity.QTeamEntity;
import team.startup.gwangjutalentfestival.domain.team.presentation.data.response.GetTeamRankingResponse;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
     * 총점 내림차순, 세부 심사 점수 평균 순으로 팀 랭킹을 조회한다.
     * 동점일 경우 완성도/표현력 점수, 창의력/구성 점수, 무대매너/퍼포먼스 점수, ID 오름차순으로 순위를 결정한다.
     *
     * @return 순위가 부여된 팀 랭킹 목록
     */
    @Override
    public List<GetTeamRankingResponse> getRanking() {
        List<String> teamNames = queryFactory
                .select(team.teamName)
                .from(team)
                .leftJoin(judgement).on(judgement.team.id.eq(team.id))
                .groupBy(team.id, team.teamName)
                .orderBy(
                        team.totalScore.desc(),
                        judgement.completenessExpressionScore.avg().desc(),
                        judgement.creativityCompositionScore.avg().desc(),
                        judgement.stagePerformanceTeamworkScore.avg().desc(),
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
