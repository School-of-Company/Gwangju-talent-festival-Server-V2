package team.startup.gwangjutalentfestival.domain.judge.util;

import java.util.Collection;

public final class JudgeScoreCalculator {

    private JudgeScoreCalculator() {
    }

    public static int calculate(Collection<Integer> scores) {
        if (scores.isEmpty()) {
            return 0;
        }
        int sum = scores.stream().mapToInt(Integer::intValue).sum();
        if (scores.size() < 3) {
            return Math.round(sum / (float) scores.size());
        }
        int min = scores.stream().mapToInt(Integer::intValue).min().orElseThrow();
        int max = scores.stream().mapToInt(Integer::intValue).max().orElseThrow();
        return Math.round((sum - min - max) / (float) (scores.size() - 2));
    }
}
