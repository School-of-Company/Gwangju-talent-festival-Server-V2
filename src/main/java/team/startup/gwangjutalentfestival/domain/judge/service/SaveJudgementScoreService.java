package team.startup.gwangjutalentfestival.domain.judge.service;

import team.startup.gwangjutalentfestival.domain.judge.presentation.data.request.SaveJudgementScoreRequest;

/**
 * 팀에 대한 심사 점수를 저장하거나 수정하는 서비스 인터페이스.
 */
public interface SaveJudgementScoreService {

    /**
     * 현재 로그인한 심사위원의 특정 팀 심사 점수를 저장하거나 수정한다.
     *
     * @param request 심사 점수 요청 데이터
     * @param teamId  대상 팀 ID
     */
    void execute(SaveJudgementScoreRequest request, Long teamId);
}
