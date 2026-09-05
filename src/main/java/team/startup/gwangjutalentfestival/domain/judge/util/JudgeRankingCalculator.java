package team.startup.gwangjutalentfestival.domain.judge.util;

import team.startup.gwangjutalentfestival.domain.judge.entity.JudgementEntity;
import team.startup.gwangjutalentfestival.domain.team.entity.TeamEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

/**
 * 팀 순위를 산출하는 유틸리티.
 * <p>1차 기준인 총점은 <b>소수 2자리(자름)</b>로 비교한다. 정수로 반올림한 값으로 정렬하면
 * 80.33 과 80.00 이 모두 80 이 되어, 실제로 점수가 더 높은 팀이 동점 처리되어 뒤로 밀린다.</p>
 * <p>소수 2자리까지 같은 진짜 동점일 때만 배점이 큰 항목(완성도 → 창의력 → 무대매너) 순으로
 * 비교하고, 그래도 같으면 팀 ID로 확정한다.</p>
 */
public final class JudgeRankingCalculator {

    private JudgeRankingCalculator() {
    }

    public static Map<Long, Integer> calculate(
            List<TeamEntity> teams,
            Collection<JudgementEntity> judgements) {
        Map<Long, List<JudgementEntity>> judgementsByTeam = judgements.stream()
                .collect(Collectors.groupingBy(judgement -> judgement.getTeam().getId()));
        List<TeamEntity> sorted = new ArrayList<>(teams);
        sorted.sort(Comparator
                .comparing(
                        (TeamEntity team) -> totalScore(
                                judgementsByTeam.getOrDefault(team.getId(), List.of())),
                        Comparator.reverseOrder())
                .thenComparing(
                        team -> average(
                                judgementsByTeam.getOrDefault(team.getId(), List.of()),
                                JudgementEntity::getCompletenessExpressionScore),
                        Comparator.reverseOrder())
                .thenComparing(
                        team -> average(
                                judgementsByTeam.getOrDefault(team.getId(), List.of()),
                                JudgementEntity::getCreativityCompositionScore),
                        Comparator.reverseOrder())
                .thenComparing(
                        team -> average(
                                judgementsByTeam.getOrDefault(team.getId(), List.of()),
                                JudgementEntity::getStagePerformanceTeamworkScore),
                        Comparator.reverseOrder())
                .thenComparing(TeamEntity::getId));

        Map<Long, Integer> result = new HashMap<>();
        for (int index = 0; index < sorted.size(); index++) {
            result.put(sorted.get(index).getId(), index + 1);
        }
        return result;
    }

    /** 표시되는 산출점수와 같은 값(소수 2자리, 자름)을 반환한다. */
    private static BigDecimal totalScore(List<JudgementEntity> judgements) {
        return JudgeScoreCalculator.calculateDecimal(judgements.stream()
                .map(judgement -> judgement.getCompletenessExpressionScore()
                        + judgement.getCreativityCompositionScore()
                        + judgement.getStagePerformanceTeamworkScore())
                .toList());
    }

    private static double average(
            List<JudgementEntity> judgements,
            ToIntFunction<JudgementEntity> score) {
        return JudgeScoreCalculator.calculateAverage(
                judgements.stream().map(score::applyAsInt).toList());
    }
}
