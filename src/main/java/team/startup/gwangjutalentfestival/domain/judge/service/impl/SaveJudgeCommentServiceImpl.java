package team.startup.gwangjutalentfestival.domain.judge.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import team.startup.gwangjutalentfestival.domain.judge.event.JudgeMonitoringChangedEvent;
import team.startup.gwangjutalentfestival.domain.judge.exception.JudgeCommentTooLargeException;
import team.startup.gwangjutalentfestival.domain.judge.presentation.data.request.SaveJudgeCommentRequest;
import team.startup.gwangjutalentfestival.domain.judge.properties.JudgeStrokesProperties;
import team.startup.gwangjutalentfestival.domain.judge.repository.JudgeCommentRepository;
import team.startup.gwangjutalentfestival.domain.judge.service.SaveJudgeCommentService;
import team.startup.gwangjutalentfestival.domain.team.entity.TeamEntity;
import team.startup.gwangjutalentfestival.domain.team.exception.TeamNotFoundException;
import team.startup.gwangjutalentfestival.domain.team.repository.TeamRepository;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.global.util.UserUtil;

import java.nio.charset.StandardCharsets;

/**
 * {@link SaveJudgeCommentService} 구현체.
 * 특정 팀에 대한 현재 심사위원의 필기 코멘트를 저장하거나 덮어쓴다(upsert).
 * strokes 구조는 해석하지 않고 전달받은 JSON을 그대로 직렬화하여 저장한다.
 * <p>필기 autosave 특성상 같은 팀/심사위원 조합으로 저장 요청이 동시에 들어올 수 있어,
 * 조회 후 분기하는 대신 DB의 유니크 제약을 이용한 원자적 upsert 쿼리를 사용한다.</p>
 */
@Service
@RequiredArgsConstructor
public class SaveJudgeCommentServiceImpl implements SaveJudgeCommentService {

    private final UserUtil userUtil;
    private final TeamRepository teamRepository;
    private final JudgeCommentRepository judgeCommentRepository;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final JudgeStrokesProperties judgeStrokesProperties;

    @Override
    @Transactional
    public void execute(SaveJudgeCommentRequest request, Long teamId) {
        UserEntity user = userUtil.getCurrentUserRef();
        TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow(TeamNotFoundException::new);

        String strokes = writeValueAsString(request.strokes());
        validateSize(strokes);

        judgeCommentRepository.upsert(team.getId(), user.getId(), strokes);
        applicationEventPublisher.publishEvent(
                JudgeMonitoringChangedEvent.commentChanged(team.getId(), user.getId()));
    }

    private String writeValueAsString(Object strokes) {
        try {
            return objectMapper.writeValueAsString(strokes);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("strokes 데이터를 직렬화할 수 없습니다.", e);
        }
    }

    private void validateSize(String strokes) {
        if (strokes.getBytes(StandardCharsets.UTF_8).length > judgeStrokesProperties.maxBytes()) {
            throw new JudgeCommentTooLargeException();
        }
    }
}
