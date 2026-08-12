package team.startup.gwangjutalentfestival.domain.performer.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

public class InvalidPerformerVerificationException extends GlobalException {
    public InvalidPerformerVerificationException() {
        super(ErrorCode.INVALID_PERFORMER_VERIFICATION);
    }
}
