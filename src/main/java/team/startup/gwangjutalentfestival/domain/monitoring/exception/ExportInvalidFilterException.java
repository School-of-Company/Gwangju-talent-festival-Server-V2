package team.startup.gwangjutalentfestival.domain.monitoring.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

public class ExportInvalidFilterException extends GlobalException {
    public ExportInvalidFilterException() {
        super(ErrorCode.EXPORT_INVALID_FILTER);
    }
}