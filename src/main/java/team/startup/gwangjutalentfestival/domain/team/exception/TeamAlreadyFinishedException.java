package team.startup.gwangjutalentfestival.domain.team.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

public class TeamAlreadyFinishedException extends GlobalException {
    public TeamAlreadyFinishedException() {
        super(ErrorCode.TEAM_ALREADY_FINISHED);
    }
}
