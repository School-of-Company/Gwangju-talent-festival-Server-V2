package team.startup.gwangjutalentfestival.domain.judge.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.startup.gwangjutalentfestival.domain.judge.exception.JudgeCommentTooLargeException;
import team.startup.gwangjutalentfestival.domain.judge.presentation.data.request.SaveJudgeCommentRequest;
import team.startup.gwangjutalentfestival.domain.judge.repository.JudgeCommentRepository;
import team.startup.gwangjutalentfestival.domain.judge.service.impl.SaveJudgeCommentServiceImpl;
import team.startup.gwangjutalentfestival.domain.team.entity.TeamEntity;
import team.startup.gwangjutalentfestival.domain.team.enums.TeamGenre;
import team.startup.gwangjutalentfestival.domain.team.enums.TeamStatus;
import team.startup.gwangjutalentfestival.domain.team.exception.TeamNotFoundException;
import team.startup.gwangjutalentfestival.domain.team.repository.TeamRepository;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;
import team.startup.gwangjutalentfestival.global.util.UserUtil;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SaveJudgeCommentServiceTest {

    private static final Long TEAM_ID = 1L;
    private static final Long USER_ID = 1L;
    private static final Long NOT_FOUND_TEAM_ID = 99L;

    @Mock
    private UserUtil userUtil;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private JudgeCommentRepository judgeCommentRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private SaveJudgeCommentServiceImpl saveJudgeCommentService;

    private UserEntity user;
    private TeamEntity team;
    private SaveJudgeCommentRequest request;

    @BeforeEach
    void setUp() {
        saveJudgeCommentService = new SaveJudgeCommentServiceImpl(
                userUtil, teamRepository, judgeCommentRepository, objectMapper);

        user = UserEntity.builder()
                .id(USER_ID)
                .role(Role.ADMIN)
                .build();

        team = TeamEntity.builder()
                .id(TEAM_ID)
                .teamName("팀A")
                .school("광주고")
                .teamStatus(TeamStatus.PENDING)
                .teamGenre(TeamGenre.SING)
                .performOrder(1)
                .totalScore(0)
                .build();

        ArrayNode strokes = objectMapper.createArrayNode();
        strokes.addObject().put("x", 1).put("y", 2);
        request = new SaveJudgeCommentRequest(strokes);
    }

    @Test
    void 팀과_심사위원_ID로_원자적_upsert가_호출된다() {
        given(userUtil.getCurrentUserRef()).willReturn(user);
        given(teamRepository.findById(TEAM_ID)).willReturn(Optional.of(team));

        saveJudgeCommentService.execute(request, TEAM_ID);

        ArgumentCaptor<String> strokesCaptor = ArgumentCaptor.forClass(String.class);
        verify(judgeCommentRepository).upsert(eq(TEAM_ID), eq(USER_ID), strokesCaptor.capture());
        assertThat(strokesCaptor.getValue()).contains("\"x\":1");
    }

    @Test
    void 존재하지_않는_팀이면_TeamNotFoundException이_발생한다() {
        given(userUtil.getCurrentUserRef()).willReturn(user);
        given(teamRepository.findById(NOT_FOUND_TEAM_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> saveJudgeCommentService.execute(request, NOT_FOUND_TEAM_ID))
                .isInstanceOf(TeamNotFoundException.class);
    }

    @Test
    void strokes가_크기_제한을_초과하면_JudgeCommentTooLargeException이_발생한다() {
        ArrayNode largeStrokes = objectMapper.createArrayNode();
        String padding = "a".repeat(1000);
        for (int i = 0; i < 600; i++) {
            largeStrokes.addObject().put("data", padding);
        }
        SaveJudgeCommentRequest largeRequest = new SaveJudgeCommentRequest(largeStrokes);

        given(userUtil.getCurrentUserRef()).willReturn(user);
        given(teamRepository.findById(TEAM_ID)).willReturn(Optional.of(team));

        assertThatThrownBy(() -> saveJudgeCommentService.execute(largeRequest, TEAM_ID))
                .isInstanceOf(JudgeCommentTooLargeException.class);
    }
}