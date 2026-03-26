package team.startup.gwangjutalentfestival.domain.judge.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

public class JudgementTotalScoreExceededException extends GlobalException {
    public JudgementTotalScoreExceededException() {
        super(ErrorCode.JUDGEMENT_TOTAL_SCORE_EXCEEDED);
    }
}
