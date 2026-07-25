package team.startup.gwangjutalentfestival.domain.judge.util;

import team.startup.gwangjutalentfestival.domain.judge.entity.JudgementEntity;
import team.startup.gwangjutalentfestival.domain.team.entity.TeamEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

public final class JudgeRankingCalculator {

    private JudgeRankingCalculator() {
    }

    public static Map<Long, Integer> calculate(
            List<TeamEntity> teams,
            Collection<JudgementEntity> judgements,
            Map<Long, Integer> calculatedScores) {
        Map<Long, List<JudgementEntity>> judgementsByTeam = judgements.stream()
                .collect(Collectors.groupingBy(judgement -> judgement.getTeam().getId()));
        List<TeamEntity> sorted = new ArrayList<>(teams);
        sorted.sort(Comparator
                .comparing(
                        (TeamEntity team) -> calculatedScores.getOrDefault(team.getId(), 0),
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

    private static double average(
            List<JudgementEntity> judgements,
            ToIntFunction<JudgementEntity> score) {
        return JudgeScoreCalculator.calculateAverage(
                judgements.stream().map(score::applyAsInt).toList());
    }
}
