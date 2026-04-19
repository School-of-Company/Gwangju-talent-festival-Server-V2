package team.startup.gwangjutalentfestival.domain.team.service;

import team.startup.gwangjutalentfestival.domain.team.presentation.data.response.GetTeamResponse;

import java.util.List;

/**
 * 전체 팀 조회 서비스 인터페이스.
 */
public interface GetAllTeamService {

    /**
     * 공연 순서 오름차순으로 전체 팀 목록을 반환한다.
     *
     * @return 전체 팀 응답 목록
     */
    List<GetTeamResponse> execute();
}
