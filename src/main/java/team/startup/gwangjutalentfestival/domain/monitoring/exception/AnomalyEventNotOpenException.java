package team.startup.gwangjutalentfestival.domain.monitoring.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

public class AnomalyEventNotOpenException extends GlobalException {
    public AnomalyEventNotOpenException() {
        super(ErrorCode.ANOMALY_EVENT_NOT_OPEN);
    }
}
