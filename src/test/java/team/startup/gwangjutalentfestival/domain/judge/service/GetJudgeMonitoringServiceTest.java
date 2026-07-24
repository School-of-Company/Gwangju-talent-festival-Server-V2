package team.startup.gwangjutalentfestival.domain.judge.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.startup.gwangjutalentfestival.domain.judge.entity.JudgeCommentEntity;
import team.startup.gwangjutalentfestival.domain.judge.entity.JudgementEntity;
import team.startup.gwangjutalentfestival.domain.judge.presentation.data.response.JudgeMonitoringResponse;
import team.startup.gwangjutalentfestival.domain.judge.repository.JudgeCommentRepository;
import team.startup.gwangjutalentfestival.domain.judge.repository.JudgementRepository;
import team.startup.gwangjutalentfestival.domain.judge.service.impl.GetJudgeMonitoringServiceImpl;
import team.startup.gwangjutalentfestival.domain.team.entity.TeamEntity;
import team.startup.gwangjutalentfestival.domain.team.enums.TeamGenre;
import team.startup.gwangjutalentfestival.domain.team.enums.TeamStatus;
import team.startup.gwangjutalentfestival.domain.team.repository.TeamRepository;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;
import team.startup.gwangjutalentfestival.domain.user.repository.UserRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GetJudgeMonitoringServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private JudgementRepository judgementRepository;
    @Mock private JudgeCommentRepository judgeCommentRepository;

    @Test
    void JUDGE_전체를_열로_두고_점수_산출점수_순위와_코멘트를_반환한다() {
        GetJudgeMonitoringService service = new GetJudgeMonitoringServiceImpl(
                userRepository, teamRepository, judgementRepository, judgeCommentRepository);
        UserEntity judgeA = user(1L, Role.JUDGE);
        UserEntity judgeB = user(2L, Role.JUDGE);
        UserEntity admin = user(3L, Role.ADMIN);
        TeamEntity teamA = team(10L, 1, "팀A");
        TeamEntity teamB = team(20L, 2, "팀B");
        ArrayNode strokes = new ObjectMapper().createArrayNode();
        strokes.addObject().put("x", 0.1);

        given(userRepository.findAllByRoleOrderByIdAsc(Role.JUDGE)).willReturn(List.of(judgeA, judgeB));
        given(teamRepository.findAllByOrderByPerformOrderAsc()).willReturn(List.of(teamA, teamB));
        given(judgementRepository.findAllWithUserAndTeam()).willReturn(List.of(
                judgement(teamA, judgeA, 40, 30, 30),
                judgement(teamA, judgeB, 20, 20, 20),
                judgement(teamA, admin, 1, 1, 1),
                judgement(teamB, judgeA, 25, 25, 20),
                judgement(teamB, judgeB, 25, 25, 20)
        ));
        given(judgeCommentRepository.findAllWithUserAndTeam()).willReturn(List.of(comment(teamA, judgeA, strokes)));

        JudgeMonitoringResponse result = service.execute();

        assertThat(result.judges()).extracting(JudgeMonitoringResponse.JudgeHeader::label)
                .containsExactly("심사위원 A", "심사위원 B");
        assertThat(result.scoreRows().getFirst().scores()).extracting(JudgeMonitoringResponse.ScoreCell::score)
                .containsExactly(100, 60);
        assertThat(result.scoreRows().getFirst().calculatedScore()).isEqualTo(80);
        assertThat(result.scoreRows().getFirst().rank()).isEqualTo(1);
        assertThat(result.scoreRows().get(1).rank()).isEqualTo(2);
        assertThat(result.commentRows().getFirst().comments()).extracting(JudgeMonitoringResponse.CommentCell::strokes)
                .containsExactly(strokes, null);
    }

    private UserEntity user(long id, Role role) {
        return UserEntity.builder().id(id).role(role).build();
    }

    private TeamEntity team(long id, int order, String name) {
        return TeamEntity.builder().id(id).teamName(name).school("광주고")
                .teamStatus(TeamStatus.PENDING).teamGenre(TeamGenre.SING).performOrder(order).totalScore(0).build();
    }

    private JudgementEntity judgement(TeamEntity team, UserEntity user, int first, int second, int third) {
        return JudgementEntity.builder().team(team).user(user).completenessExpressionScore(first)
                .creativityCompositionScore(second).stagePerformanceTeamworkScore(third).build();
    }

    private JudgeCommentEntity comment(TeamEntity team, UserEntity user, ArrayNode strokes) {
        return JudgeCommentEntity.builder().team(team).user(user).strokes(strokes).build();
    }
}
