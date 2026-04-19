package team.startup.gwangjutalentfestival.domain.judge.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.judge.entity.JudgementEntity;
import team.startup.gwangjutalentfestival.domain.judge.mapper.JudgementMapper;
import team.startup.gwangjutalentfestival.domain.judge.presentation.data.response.GetJudgementResponse;
import team.startup.gwangjutalentfestival.domain.judge.repository.JudgementRepository;
import team.startup.gwangjutalentfestival.domain.judge.service.GetJudgementService;
import team.startup.gwangjutalentfestival.domain.team.entity.TeamEntity;
import team.startup.gwangjutalentfestival.domain.team.exception.TeamNotFoundException;
import team.startup.gwangjutalentfestival.domain.team.repository.TeamRepository;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.global.util.UserUtil;

/**
 * {@link GetJudgementService} 구현체.
 * 특정 팀에 대한 현재 심사위원의 심사 데이터를 조회하여 반환한다.
 */
@Service
@RequiredArgsConstructor
public class GetJudgementServiceImpl implements GetJudgementService {
    private final UserUtil userUtil;
    private final TeamRepository teamRepository;
    private final JudgementRepository judgementRepository;

    /**
     * 현재 로그인한 심사위원의 특정 팀 심사를 조회한다.
     * 심사 데이터가 없으면 기본 점수로 응답을 구성한다.
     *
     * @param teamId 조회할 팀 ID
     * @return 단일 팀 심사 응답
     */
    @Override
    @Transactional(readOnly = true)
    public GetJudgementResponse execute(Long teamId) {
        UserEntity user = userUtil.getCurrentUser();
        TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow(TeamNotFoundException::new);

        JudgementEntity judgement = judgementRepository.findByTeamAndUser(team, user)
                .orElse(null);

        return JudgementMapper.toResponse(team, judgement);
    }
}
