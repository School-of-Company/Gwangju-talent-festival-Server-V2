package team.startup.gwangjutalentfestival.domain.team.exception;

import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.GlobalException;

/**
 * 이미 공연 진행 중인 팀에 중복 처리를 시도할 때 발생하는 예외.
 */
public class TeamAlreadyOngoingException extends GlobalException {
    public TeamAlreadyOngoingException() {
        super(ErrorCode.TEAM_ALREADY_ONGOING);
    }
}
