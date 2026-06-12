package team.startup.gwangjutalentfestival.domain.monitoring.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

public class ExportFailedException extends GlobalException {
    public ExportFailedException() {
        super(ErrorCode.EXPORT_FAILED);
    }
}