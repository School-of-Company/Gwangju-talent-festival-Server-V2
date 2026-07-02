package team.startup.gwangjutalentfestival.domain.judge.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.startup.gwangjutalentfestival.domain.judge.entity.JudgeCommentEntity;
import team.startup.gwangjutalentfestival.domain.judge.presentation.data.response.GetJudgeCommentResponse;
import team.startup.gwangjutalentfestival.domain.judge.repository.JudgeCommentRepository;
import team.startup.gwangjutalentfestival.domain.judge.service.impl.GetJudgeCommentServiceImpl;
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
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GetJudgeCommentServiceTest {

    private static final Long TEAM_ID = 1L;
    private static final Long NOT_FOUND_TEAM_ID = 99L;

    @Mock
    private UserUtil userUtil;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private JudgeCommentRepository judgeCommentRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private GetJudgeCommentServiceImpl getJudgeCommentService;

    private UserEntity user;
    private TeamEntity team;

    @BeforeEach
    void setUp() {
        getJudgeCommentService = new GetJudgeCommentServiceImpl(
                userUtil, teamRepository, judgeCommentRepository, objectMapper);

        user = UserEntity.builder()
                .id(1L)
                .role(Role.ADMIN)
                .build();

        team = TeamEntity.builder()
                .id(TEAM_ID)
                .teamName("팀A")
                .school("광주고")
                .teamStatus(TeamStatus.PENDING)
                .teamGenre(TeamGenre.SING)
                .performOrder(1)
                .totalScore(50)
                .build();
    }

    @Test
    void 코멘트가_있으면_저장된_strokes가_반환된다() {
        JudgeCommentEntity comment = JudgeCommentEntity.builder()
                .id(10L)
                .strokes("[{\"x\":1,\"y\":2}]")
                .team(team)
                .user(user)
                .build();

        given(userUtil.getCurrentUserRef()).willReturn(user);
        given(teamRepository.findById(TEAM_ID)).willReturn(Optional.of(team));
        given(judgeCommentRepository.findByTeamAndUser(team, user)).willReturn(Optional.of(comment));

        GetJudgeCommentResponse response = getJudgeCommentService.execute(TEAM_ID);

        assertThat(response.teamId()).isEqualTo(TEAM_ID);
        assertThat(response.strokes().get(0).get("x").asInt()).isEqualTo(1);
    }

    @Test
    void 코멘트가_없으면_빈_배열이_반환된다() {
        given(userUtil.getCurrentUserRef()).willReturn(user);
        given(teamRepository.findById(TEAM_ID)).willReturn(Optional.of(team));
        given(judgeCommentRepository.findByTeamAndUser(team, user)).willReturn(Optional.empty());

        GetJudgeCommentResponse response = getJudgeCommentService.execute(TEAM_ID);

        JsonNode strokes = response.strokes();
        assertThat(strokes.isArray()).isTrue();
        assertThat(strokes).isEmpty();
    }

    @Test
    void 존재하지_않는_팀이면_TeamNotFoundException이_발생한다() {
        given(userUtil.getCurrentUserRef()).willReturn(user);
        given(teamRepository.findById(NOT_FOUND_TEAM_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> getJudgeCommentService.execute(NOT_FOUND_TEAM_ID))
                .isInstanceOf(TeamNotFoundException.class);
    }
}
