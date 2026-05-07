package team.startup.gwangjutalentfestival.domain.judge.service;

import team.startup.gwangjutalentfestival.domain.judge.presentation.data.response.GetJudgementResponse;

import java.util.List;

/**
 * 전체 팀에 대한 현재 심사위원의 심사 목록을 조회하는 서비스 인터페이스.
 */
public interface GetAllJudgementService {

    /**
     * 현재 로그인한 심사위원의 전체 팀 심사 목록을 반환한다.
     *
     * @return 전체 팀 심사 응답 목록
     */
    List<GetJudgementResponse> execute();
}
