package team.startup.gwangjutalentfestival.domain.team.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

public class TeamAlreadyOngoingException extends GlobalException {
    public TeamAlreadyOngoingException() {
        super(ErrorCode.TEAM_ALREADY_ONGOING);
    }
}
