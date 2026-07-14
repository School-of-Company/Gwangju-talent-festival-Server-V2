package team.startup.gwangjutalentfestival.domain.judge.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.startup.gwangjutalentfestival.domain.judge.entity.JudgementEntity;
import team.startup.gwangjutalentfestival.domain.judge.presentation.data.response.GetJudgementResponse;
import team.startup.gwangjutalentfestival.domain.judge.repository.JudgementRepository;
import team.startup.gwangjutalentfestival.domain.judge.service.impl.GetAllJudgementServiceImpl;
import team.startup.gwangjutalentfestival.domain.team.entity.TeamEntity;
import team.startup.gwangjutalentfestival.domain.team.enums.TeamGenre;
import team.startup.gwangjutalentfestival.domain.team.enums.TeamStatus;
import team.startup.gwangjutalentfestival.domain.team.repository.TeamRepository;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;
import team.startup.gwangjutalentfestival.global.util.UserUtil;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GetAllJudgementServiceTest {

    @Mock
    private JudgementRepository judgementRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private UserUtil userUtil;

    @InjectMocks
    private GetAllJudgementServiceImpl getAllJudgementService;

    private UserEntity user;
    private TeamEntity team1;
    private TeamEntity team2;

    @BeforeEach
    void setUp() {
        user = UserEntity.builder()
                .id(1L)
                .role(Role.ADMIN)
                .build();

        team1 = TeamEntity.builder()
                .id(1L)
                .teamName("팀A")
                .school("광주고")
                .teamStatus(TeamStatus.ONGOING)
                .teamGenre(TeamGenre.SING)
                .performOrder(1)
                .totalScore(60)
                .build();

        team2 = TeamEntity.builder()
                .id(2L)
                .teamName("팀B")
                .school("광주고")
                .teamStatus(TeamStatus.PENDING)
                .teamGenre(TeamGenre.DANCE)
                .performOrder(2)
                .totalScore(0)
                .build();
    }

    @Test
    void 심사_기록이_있는_팀은_실제_점수가_반환된다() {
        JudgementEntity judgement = JudgementEntity.builder()
                .id(10L)
                .completenessExpressionScore(30)
                .creativityCompositionScore(15)
                .stagePerformanceTeamworkScore(10)
                .team(team1)
                .user(user)
                .build();

        given(userUtil.getCurrentUser()).willReturn(user);
        given(judgementRepository.findAllByUser(user)).willReturn(List.of(judgement));
        given(teamRepository.findAll()).willReturn(List.of(team1, team2));

        List<GetJudgementResponse> result = getAllJudgementService.execute();

        GetJudgementResponse team1Response = result.stream()
                .filter(r -> r.teamId().equals(1L))
                .findFirst().orElseThrow();

        assertThat(team1Response.judgementId()).isEqualTo(10L);
        assertThat(team1Response.completenessExpressionScore()).isEqualTo(30);
        assertThat(team1Response.creativityCompositionScore()).isEqualTo(15);
        assertThat(team1Response.isJudged()).isTrue();
    }

    @Test
    void 심사_기록이_없는_팀은_기본_점수가_반환된다() {
        given(userUtil.getCurrentUser()).willReturn(user);
        given(judgementRepository.findAllByUser(user)).willReturn(List.of());
        given(teamRepository.findAll()).willReturn(List.of(team2));

        List<GetJudgementResponse> result = getAllJudgementService.execute();

        GetJudgementResponse team2Response = result.get(0);
        assertThat(team2Response.judgementId()).isNull();
        assertThat(team2Response.completenessExpressionScore()).isEqualTo(40);
        assertThat(team2Response.creativityCompositionScore()).isEqualTo(30);
        assertThat(team2Response.stagePerformanceTeamworkScore()).isEqualTo(30);
        assertThat(team2Response.isJudged()).isFalse();
    }

    @Test
    void PENDING_팀은_isPerformed가_false이고_그외_팀은_true이다() {
        given(userUtil.getCurrentUser()).willReturn(user);
        given(judgementRepository.findAllByUser(user)).willReturn(List.of());
        given(teamRepository.findAll()).willReturn(List.of(team1, team2));

        List<GetJudgementResponse> result = getAllJudgementService.execute();

        GetJudgementResponse ongoingResponse = result.stream()
                .filter(r -> r.teamId().equals(1L)).findFirst().orElseThrow();
        GetJudgementResponse pendingResponse = result.stream()
                .filter(r -> r.teamId().equals(2L)).findFirst().orElseThrow();

        assertThat(ongoingResponse.isPerformed()).isTrue();
        assertThat(pendingResponse.isPerformed()).isFalse();
    }

    @Test
    void 팀_목록이_비어있으면_빈_리스트가_반환된다() {
        given(userUtil.getCurrentUser()).willReturn(user);
        given(judgementRepository.findAllByUser(user)).willReturn(List.of());
        given(teamRepository.findAll()).willReturn(List.of());

        List<GetJudgementResponse> result = getAllJudgementService.execute();

        assertThat(result).isEmpty();
    }

    @Test
    void 반환_목록_크기가_전체_팀_수와_동일하다() {
        given(userUtil.getCurrentUser()).willReturn(user);
        given(judgementRepository.findAllByUser(user)).willReturn(List.of());
        given(teamRepository.findAll()).willReturn(List.of(team1, team2));

        List<GetJudgementResponse> result = getAllJudgementService.execute();

        assertThat(result).hasSize(2);
    }
}
