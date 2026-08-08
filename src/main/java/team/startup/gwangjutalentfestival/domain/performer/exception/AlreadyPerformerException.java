package team.startup.gwangjutalentfestival.domain.performer.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

public class AlreadyPerformerException extends GlobalException {
    public AlreadyPerformerException() {
        super(ErrorCode.ALREADY_PERFORMER);
    }
}
