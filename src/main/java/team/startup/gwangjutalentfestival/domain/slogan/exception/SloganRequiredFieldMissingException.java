package team.startup.gwangjutalentfestival.domain.slogan.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

public class SloganRequiredFieldMissingException extends GlobalException {
    public SloganRequiredFieldMissingException() {
        super(ErrorCode.SLOGAN_REQUIRED_FIELD_MISSING);
    }
}