package team.startup.gwangjutalentfestival.domain.user.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

public class NotFoundMemberException extends GlobalException {
    public NotFoundMemberException() {
        super(ErrorCode.USER_NOT_FOUND);
    }
}
