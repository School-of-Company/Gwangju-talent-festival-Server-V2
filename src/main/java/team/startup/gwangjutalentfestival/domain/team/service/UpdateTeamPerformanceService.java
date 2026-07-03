package team.startup.gwangjutalentfestival.domain.team.service;

/**
 * 팀 공연 상태 변경 서비스 인터페이스.
 */
public interface UpdateTeamPerformanceService {

    /**
     * 특정 팀의 공연 상태를 다음 단계로 변경한다.
     *
     * @param teamId 상태를 변경할 팀 ID
     */
    void execute(Long teamId);
}
