package team.startup.gwangjutalentfestival.domain.apply.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

public class ApplyNotFoundException extends GlobalException {
    public ApplyNotFoundException() {
        super(ErrorCode.APPLY_NOT_FOUND);
    }
}
