package team.startup.gwangjutalentfestival.domain.slogan.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

public class SloganSubmissionPeriodException extends GlobalException {
    public SloganSubmissionPeriodException() {
        super(ErrorCode.SLOGAN_SUBMISSION_PERIOD_INVALID);
    }
}
