package team.startup.gwangjutalentfestival.domain.team.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

/**
 * 요청한 팀이 존재하지 않을 때 발생하는 예외.
 */
public class TeamNotFoundException extends GlobalException {
    public TeamNotFoundException() {
        super(ErrorCode.TEAM_NOT_FOUND);
    }
}
