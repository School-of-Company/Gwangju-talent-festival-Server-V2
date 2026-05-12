package team.startup.gwangjutalentfestival.domain.slogan.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

public class InvalidSloganAgeException extends GlobalException {
    public InvalidSloganAgeException() {
        super(ErrorCode.INVALID_AGE);
    }
}