package team.startup.gwangjutalentfestival.domain.judge.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.judge.entity.JudgeCommentEntity;
import team.startup.gwangjutalentfestival.domain.judge.presentation.data.response.GetJudgeCommentResponse;
import team.startup.gwangjutalentfestival.domain.judge.repository.JudgeCommentRepository;
import team.startup.gwangjutalentfestival.domain.judge.service.GetJudgeCommentService;
import team.startup.gwangjutalentfestival.domain.team.entity.TeamEntity;
import team.startup.gwangjutalentfestival.domain.team.exception.TeamNotFoundException;
import team.startup.gwangjutalentfestival.domain.team.repository.TeamRepository;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.global.util.UserUtil;

/**
 * {@link GetJudgeCommentService} 구현체.
 * 특정 팀에 대한 현재 심사위원의 필기 코멘트를 조회하여 반환한다.
 * strokes 구조는 해석하지 않고 저장된 JSON을 그대로 반환한다.
 */
@Service
@RequiredArgsConstructor
public class GetJudgeCommentServiceImpl implements GetJudgeCommentService {
    private final UserUtil userUtil;
    private final TeamRepository teamRepository;
    private final JudgeCommentRepository judgeCommentRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public GetJudgeCommentResponse execute(Long teamId) {
        UserEntity user = userUtil.getCurrentUserRef();
        TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow(TeamNotFoundException::new);

        JsonNode strokes = judgeCommentRepository.findByTeamAndUser(team, user)
                .map(JudgeCommentEntity::getStrokes)
                .filter(node -> node != null && !node.isNull())
                .orElseGet(objectMapper::createArrayNode);

        return new GetJudgeCommentResponse(teamId, strokes);
    }
}