package team.startup.gwangjutalentfestival.domain.team.repository;

import team.startup.gwangjutalentfestival.domain.team.presentation.data.response.GetTeamRankingResponse;

import java.util.List;

/**
 * QueryDSL을 활용한 팀 커스텀 쿼리 인터페이스.
 */
public interface TeamRepositoryCustom {

    /**
     * 총점 및 세부 심사 점수 기준으로 팀 랭킹을 조회한다.
     *
     * @return 순위가 포함된 팀 랭킹 목록
     */
    List<GetTeamRankingResponse> getRanking();
}
