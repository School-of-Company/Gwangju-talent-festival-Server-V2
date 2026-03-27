package team.startup.gwangjutalentfestival.domain.judge.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.judge.entity.JudgementEntity;
import team.startup.gwangjutalentfestival.domain.judge.mapper.JudgementMapper;
import team.startup.gwangjutalentfestival.domain.judge.presentation.data.response.GetJudgementResponse;
import team.startup.gwangjutalentfestival.domain.judge.repository.JudgementRepository;
import team.startup.gwangjutalentfestival.domain.judge.service.GetAllJudgementService;
import team.startup.gwangjutalentfestival.domain.team.entity.TeamEntity;
import team.startup.gwangjutalentfestival.domain.team.repository.TeamRepository;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.global.util.UserUtil;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetAllJudgementServiceImpl implements GetAllJudgementService {
    private static final int DEFAULT_EXPRESSION_COMMUNICATION_SCORE = 40;
    private static final int DEFAULT_TECHNICAL_COMPLETENESS_SCORE = 30;
    private static final int DEFAULT_CREATIVITY_COMPOSITION_SCORE = 30;
    private static final int DEFAULT_STAGE_PRESENCE_PERFORMANCE_SCORE = 30;
    private static final int DEFAULT_TEAMWORK_STAGE_HARMONY_SCORE = 30;

    private final JudgementRepository judgementRepository;
    private final TeamRepository teamRepository;
    private final UserUtil userUtil;

    @Override
    @Transactional(readOnly = true)
    public List<GetJudgementResponse> execute() {
        UserEntity user = userUtil.getCurrentUser();
        List<JudgementEntity> judgement = judgementRepository.findAllByUser(user);
        List<TeamEntity> teams = teamRepository.findAll();

        Map<Long, JudgementEntity> judgementMap = judgement.stream()
                .collect(Collectors.toMap(j -> j.getTeam().getId(), j -> j));

        return teams.stream()
                .map(team -> JudgementMapper.toResponse(team,judgementMap.get(team.getId())))
                .toList();
    }
}
