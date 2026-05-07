package team.startup.gwangjutalentfestival.domain.team.enums;

import team.startup.gwangjutalentfestival.domain.team.exception.TeamAlreadyFinishedException;

/**
 * 팀의 공연 진행 상태를 정의하는 열거형.
 * <ul>
 *   <li>{@code PENDING} - 공연 대기 중</li>
 *   <li>{@code ONGOING} - 공연 진행 중</li>
 *   <li>{@code FINISHED} - 공연 완료</li>
 * </ul>
 */
public enum TeamStatus {
    PENDING, ONGOING, FINISHED;

    /**
     * 현재 상태에서 다음 상태로 전환한다.
     * {@code FINISHED} 상태에서 호출하면 {@link team.startup.gwangjutalentfestival.domain.team.exception.TeamAlreadyFinishedException}이 발생한다.
     *
     * @return 다음 공연 상태
     */
    public TeamStatus next() {
        return switch (this) {
            case PENDING -> ONGOING;
            case ONGOING -> FINISHED;
            case FINISHED -> throw new TeamAlreadyFinishedException();
        };
    }
}
