package team.startup.gwangjutalentfestival.domain.monitoring.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

public class ExportPeriodExceededException extends GlobalException {
    public ExportPeriodExceededException() {
        super(ErrorCode.EXPORT_PERIOD_EXCEEDED);
    }
}