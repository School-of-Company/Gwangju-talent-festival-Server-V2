package team.startup.gwangjutalentfestival.domain.monitoring.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

public class FeedbackAlreadyExistsException extends GlobalException {
    public FeedbackAlreadyExistsException() {
        super(ErrorCode.FEEDBACK_ALREADY_EXISTS);
    }
}
