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

/**
 * {@link GetAllJudgementService} 구현체.
 * 모든 팀 목록과 현재 심사위원의 심사 데이터를 결합하여 응답을 반환한다.
 */
@Service
@RequiredArgsConstructor
public class GetAllJudgementServiceImpl implements GetAllJudgementService {
    private final JudgementRepository judgementRepository;
    private final TeamRepository teamRepository;
    private final UserUtil userUtil;

    /**
     * 현재 로그인한 심사위원의 전체 팀 심사 목록을 반환한다.
     * 심사 데이터가 없는 팀은 기본 점수로 응답을 구성한다.
     *
     * @return 전체 팀 심사 응답 목록
     */
    @Override
    @Transactional(readOnly = true)
    public List<GetJudgementResponse> execute() {
        UserEntity user = userUtil.getCurrentUser();
        List<JudgementEntity> judgements = judgementRepository.findAllByUser(user);
        List<TeamEntity> teams = teamRepository.findAll();

        Map<Long, JudgementEntity> judgementMap = judgements.stream()
                .collect(Collectors.toMap(j -> j.getTeam().getId(), j -> j));

        return teams.stream()
                .map(team -> JudgementMapper.toResponse(team, judgementMap.get(team.getId())))
                .toList();
    }
}
