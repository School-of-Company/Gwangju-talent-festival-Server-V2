package team.startup.gwangjutalentfestival.domain.team.service;

import team.startup.gwangjutalentfestival.domain.team.presentation.data.response.GetTeamRankingResponse;

import java.util.List;

/**
 * 팀 랭킹 조회 서비스 인터페이스.
 */
public interface GetTeamRankingService {

    /**
     * 총점 기준으로 팀 랭킹 목록을 반환한다.
     *
     * @return 순위가 포함된 팀 랭킹 응답 목록
     */
    List<GetTeamRankingResponse> execute();
}
