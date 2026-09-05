package team.startup.gwangjutalentfestival.domain.judge.util;

import org.junit.jupiter.api.Test;
import team.startup.gwangjutalentfestival.domain.judge.entity.JudgementEntity;
import team.startup.gwangjutalentfestival.domain.team.entity.TeamEntity;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JudgeRankingCalculatorTest {

    @Test
    void 단순평균이_낮아도_완성도_최고최저_제외평균이_높은_팀이_앞선다() {
        TeamEntity teamA = team(2L);
        TeamEntity teamB = team(1L);
        List<JudgementEntity> judgements = new ArrayList<>();
        int[] teamACompleteness = {0, 20, 20, 20};
        int[] teamBCompleteness = {18, 18, 18, 30};
        for (int index = 0; index < 4; index++) {
            UserEntity judge = judge(index + 1L);
            judgements.add(judgement(teamA, judge, teamACompleteness[index], 10, 10));
            judgements.add(judgement(teamB, judge, teamBCompleteness[index], 10, 10));
        }

        Map<Long, Integer> ranks = JudgeRankingCalculator.calculate(
                List.of(teamA, teamB), judgements);

        assertThat(ranks).containsEntry(teamA.getId(), 1).containsEntry(teamB.getId(), 2);
    }

    @Test
    void 완성도가_같으면_창의력_이후_무대점수와_팀ID_순으로_비교한다() {
        TeamEntity teamA = team(3L);
        TeamEntity teamB = team(2L);
        TeamEntity teamC = team(1L);
        UserEntity judge = judge(1L);
        List<JudgementEntity> judgements = List.of(
                judgement(teamA, judge, 20, 11, 10),
                judgement(teamB, judge, 20, 10, 11),
                judgement(teamC, judge, 20, 10, 11)
        );

        Map<Long, Integer> ranks = JudgeRankingCalculator.calculate(
                List.of(teamA, teamB, teamC), judgements);

        assertThat(ranks)
                .containsEntry(teamA.getId(), 1)
                .containsEntry(teamC.getId(), 2)
                .containsEntry(teamB.getId(), 3);
    }

    @Test
    void 정수로_반올림하면_동점이지만_소수점까지_보면_높은_팀이_1위가_된다() {
        TeamEntity teamA = team(1L);
        TeamEntity teamB = team(2L);
        int[] teamATotals = {80, 86, 77, 80, 80};
        int[] teamBTotals = {81, 90, 65, 75, 85};
        List<JudgementEntity> judgements = new ArrayList<>();
        for (int index = 0; index < 5; index++) {
            UserEntity judge = judge(index + 1L);
            judgements.add(judgement(teamA, judge, 40, teamATotals[index] - 60, 20));
            judgements.add(judgement(teamB, judge, 40, teamBTotals[index] - 60, 20));
        }

        Map<Long, Integer> ranks = JudgeRankingCalculator.calculate(
                List.of(teamA, teamB), judgements);

        assertThat(ranks).containsEntry(teamB.getId(), 1).containsEntry(teamA.getId(), 2);
    }

    private TeamEntity team(long id) {
        return TeamEntity.builder().id(id).totalScore(70).build();
    }

    private UserEntity judge(long id) {
        return UserEntity.builder().id(id).role(Role.JUDGE).build();
    }

    private JudgementEntity judgement(
            TeamEntity team,
            UserEntity judge,
            int completeness,
            int creativity,
            int stage) {
        return JudgementEntity.builder()
                .team(team)
                .user(judge)
                .completenessExpressionScore(completeness)
                .creativityCompositionScore(creativity)
                .stagePerformanceTeamworkScore(stage)
                .build();
    }
}
