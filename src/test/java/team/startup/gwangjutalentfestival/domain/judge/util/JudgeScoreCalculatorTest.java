package team.startup.gwangjutalentfestival.domain.judge.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JudgeScoreCalculatorTest {

    @Test
    void 세건_이상이면_최고점과_최저점_한건씩_제외한_실수_평균을_계산한다() {
        assertThat(JudgeScoreCalculator.calculateAverage(List.of(0, 10, 11, 20)))
                .isEqualTo(10.5);
    }

    @Test
    void 최고점과_최저점이_동점이어도_한건씩만_제외한다() {
        assertThat(JudgeScoreCalculator.calculateAverage(List.of(0, 0, 10, 10)))
                .isEqualTo(5);
    }

    @Test
    void 두건_이하면_전체_평균을_사용하고_빈값은_0이다() {
        assertThat(JudgeScoreCalculator.calculateAverage(List.of())).isZero();
        assertThat(JudgeScoreCalculator.calculateAverage(List.of(7))).isEqualTo(7);
        assertThat(JudgeScoreCalculator.calculateAverage(List.of(10, 11))).isEqualTo(10.5);
    }

    @Test
    void 산출점수는_제외_평균을_정수로_반올림한다() {
        assertThat(JudgeScoreCalculator.calculate(List.of(0, 10, 11, 20))).isEqualTo(11);
    }
}
