
package team.startup.gwangjutalentfestival.domain.monitoring.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

public class ExportInvalidPeriodException extends GlobalException {
    public ExportInvalidPeriodException() {
        super(ErrorCode.EXPORT_INVALID_PERIOD);
    }
}