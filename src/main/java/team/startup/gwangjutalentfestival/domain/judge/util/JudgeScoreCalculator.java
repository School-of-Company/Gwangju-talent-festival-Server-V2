package team.startup.gwangjutalentfestival.domain.judge.util;

import java.util.Collection;

public final class JudgeScoreCalculator {

    private JudgeScoreCalculator() {
    }

    public static int calculate(Collection<Integer> scores) {
        return (int) Math.round(calculateAverage(scores));
    }

    public static double calculateAverage(Collection<Integer> scores) {
        if (scores.isEmpty()) {
            return 0;
        }
        int sum = scores.stream().mapToInt(Integer::intValue).sum();
        if (scores.size() < 3) {
            return sum / (double) scores.size();
        }
        int min = scores.stream().mapToInt(Integer::intValue).min().orElseThrow();
        int max = scores.stream().mapToInt(Integer::intValue).max().orElseThrow();
        return (sum - min - max) / (double) (scores.size() - 2);
    }
}
