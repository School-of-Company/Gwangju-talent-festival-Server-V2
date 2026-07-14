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
import team.startup.gwangjutalentfestival.domain.judge.service.impl.GetJudgementServiceImpl;
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
class GetJudgementServiceTest {

    private static final Long TEAM_ID = 1L;
    private static final Long NOT_FOUND_TEAM_ID = 99L;

    @Mock
    private UserUtil userUtil;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private JudgementRepository judgementRepository;

    @InjectMocks
    private GetJudgementServiceImpl getJudgementService;

    private UserEntity user;
    private TeamEntity pendingTeam;
    private TeamEntity performingTeam;

    @BeforeEach
    void setUp() {
        user = UserEntity.builder()
                .id(1L)
                .role(Role.ADMIN)
                .build();

        pendingTeam = TeamEntity.builder()
                .id(TEAM_ID)
                .teamName("팀A")
                .school("광주고")
                .teamStatus(TeamStatus.PENDING)
                .teamGenre(TeamGenre.SING)
                .performOrder(1)
                .totalScore(50)
                .build();

        performingTeam = TeamEntity.builder()
                .id(TEAM_ID)
                .teamName("팀B")
                .school("광주고")
                .teamStatus(TeamStatus.ONGOING)
                .teamGenre(TeamGenre.DANCE)
                .performOrder(2)
                .totalScore(80)
                .build();
    }

    @Test
    void 심사_기록이_있으면_실제_점수가_반환된다() {
        JudgementEntity judgement = JudgementEntity.builder()
                .id(10L)
                .completenessExpressionScore(20)
                .creativityCompositionScore(10)
                .stagePerformanceTeamworkScore(5)
                .team(pendingTeam)
                .user(user)
                .build();

        given(userUtil.getCurrentUser()).willReturn(user);
        given(teamRepository.findById(TEAM_ID)).willReturn(Optional.of(pendingTeam));
        given(judgementRepository.findByTeamAndUser(pendingTeam, user)).willReturn(Optional.of(judgement));

        GetJudgementResponse response = getJudgementService.execute(TEAM_ID);

        assertThat(response.judgementId()).isEqualTo(10L);
        assertThat(response.teamId()).isEqualTo(TEAM_ID);
        assertThat(response.teamName()).isEqualTo("팀A");
        assertThat(response.completenessExpressionScore()).isEqualTo(20);
        assertThat(response.creativityCompositionScore()).isEqualTo(10);
        assertThat(response.stagePerformanceTeamworkScore()).isEqualTo(5);
        assertThat(response.isJudged()).isTrue();
    }

    @Test
    void 심사_기록이_없으면_기본값이_반환된다() {
        given(userUtil.getCurrentUser()).willReturn(user);
        given(teamRepository.findById(TEAM_ID)).willReturn(Optional.of(pendingTeam));
        given(judgementRepository.findByTeamAndUser(pendingTeam, user)).willReturn(Optional.empty());

        GetJudgementResponse response = getJudgementService.execute(TEAM_ID);

        assertThat(response.judgementId()).isNull();
        assertThat(response.completenessExpressionScore()).isEqualTo(40);
        assertThat(response.creativityCompositionScore()).isEqualTo(30);
        assertThat(response.stagePerformanceTeamworkScore()).isEqualTo(30);
        assertThat(response.isJudged()).isFalse();
    }

    @Test
    void PENDING_상태_팀은_isPerformed가_false이다() {
        given(userUtil.getCurrentUser()).willReturn(user);
        given(teamRepository.findById(TEAM_ID)).willReturn(Optional.of(pendingTeam));
        given(judgementRepository.findByTeamAndUser(pendingTeam, user)).willReturn(Optional.empty());

        GetJudgementResponse response = getJudgementService.execute(TEAM_ID);

        assertThat(response.isPerformed()).isFalse();
    }

    @Test
    void ONGOING_상태_팀은_isPerformed가_true이다() {
        given(userUtil.getCurrentUser()).willReturn(user);
        given(teamRepository.findById(TEAM_ID)).willReturn(Optional.of(performingTeam));
        given(judgementRepository.findByTeamAndUser(performingTeam, user)).willReturn(Optional.empty());

        GetJudgementResponse response = getJudgementService.execute(TEAM_ID);

        assertThat(response.isPerformed()).isTrue();
    }

    @Test
    void totalScore가_null이면_0으로_반환된다() {
        TeamEntity teamWithNullScore = TeamEntity.builder()
                .id(TEAM_ID)
                .teamName("팀C")
                .school("광주고")
                .teamStatus(TeamStatus.PENDING)
                .teamGenre(TeamGenre.SING)
                .performOrder(1)
                .totalScore(null)
                .build();

        given(userUtil.getCurrentUser()).willReturn(user);
        given(teamRepository.findById(TEAM_ID)).willReturn(Optional.of(teamWithNullScore));
        given(judgementRepository.findByTeamAndUser(teamWithNullScore, user)).willReturn(Optional.empty());

        GetJudgementResponse response = getJudgementService.execute(TEAM_ID);

        assertThat(response.totalScore()).isEqualTo(0);
    }

    @Test
    void 존재하지_않는_팀ID이면_TeamNotFoundException이_발생한다() {
        given(userUtil.getCurrentUser()).willReturn(user);
        given(teamRepository.findById(NOT_FOUND_TEAM_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> getJudgementService.execute(NOT_FOUND_TEAM_ID))
                .isInstanceOf(TeamNotFoundException.class);
    }
}
