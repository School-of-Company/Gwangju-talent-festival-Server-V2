package team.startup.gwangjutalentfestival.domain.team.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

/**
 * 이미 공연이 완료된 팀의 상태를 변경하려 할 때 발생하는 예외.
 */
public class TeamAlreadyFinishedException extends GlobalException {
    public TeamAlreadyFinishedException() {
        super(ErrorCode.TEAM_ALREADY_FINISHED);
    }
}
