package team.startup.gwangjutalentfestival.domain.slogan.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

/**
 * 슬로건 접수 기간이 아닐 때 발생하는 예외.
 * 현재 시각이 설정된 접수 시작일 이전이거나 종료일 이후인 경우 발생한다.
 */
public class SloganSubmissionPeriodException extends GlobalException {
    public SloganSubmissionPeriodException() {
        super(ErrorCode.SLOGAN_SUBMISSION_PERIOD_INVALID);
    }
}
