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

@Service
@RequiredArgsConstructor
public class GetJudgementServiceImpl implements GetJudgementService {
    private final UserUtil userUtil;
    private final TeamRepository teamRepository;
    private final JudgementRepository judgementRepository;

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
