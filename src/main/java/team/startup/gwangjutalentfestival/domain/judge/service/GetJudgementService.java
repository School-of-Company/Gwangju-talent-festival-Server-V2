package team.startup.gwangjutalentfestival.domain.judge.service;

import team.startup.gwangjutalentfestival.domain.judge.presentation.data.response.GetJudgementResponse;

/**
 * 특정 팀에 대한 현재 심사위원의 심사를 조회하는 서비스 인터페이스.
 */
public interface GetJudgementService {

    /**
     * 현재 로그인한 심사위원의 특정 팀 심사를 조회한다.
     *
     * @param teamId 조회할 팀 ID
     * @return 단일 팀 심사 응답
     */
    GetJudgementResponse execute(Long teamId);
}
