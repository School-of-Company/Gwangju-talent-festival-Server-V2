package team.startup.gwangjutalentfestival.domain.judge.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

/**
 * 심사 점수의 합계가 허용된 최대 점수(100점)를 초과할 때 발생하는 예외.
 */
public class JudgementTotalScoreExceededException extends GlobalException {
    public JudgementTotalScoreExceededException() {
        super(ErrorCode.JUDGEMENT_TOTAL_SCORE_EXCEEDED);
    }
}
