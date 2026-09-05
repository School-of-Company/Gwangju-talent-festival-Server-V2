package team.startup.gwangjutalentfestival.domain.judge.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;

public final class JudgeScoreCalculator {

    private static final int SCORE_SCALE = 2;

    private JudgeScoreCalculator() {
    }

    /**
     * 엑셀 산출점수·순위 비교에 쓰는 값.
     * 반올림하지 않고 소수 3째 자리에서 잘라 2자리로 만든다(81.666... -> 81.66).
     */
    public static BigDecimal calculateDecimal(Collection<Integer> scores) {
        return BigDecimal.valueOf(calculateAverage(scores))
                .setScale(SCORE_SCALE, RoundingMode.DOWN);
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
