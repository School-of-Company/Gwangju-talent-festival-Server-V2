package team.startup.gwangjutalentfestival.domain.team.enums;

import team.startup.gwangjutalentfestival.domain.team.exception.TeamAlreadyFinishedException;

public enum TeamStatus {
    PENDING, ONGOING, FINISHED;

    public TeamStatus next() {
        return switch (this) {
            case PENDING -> ONGOING;
            case ONGOING -> FINISHED;
            case FINISHED -> throw new TeamAlreadyFinishedException();
        };
    }
}
