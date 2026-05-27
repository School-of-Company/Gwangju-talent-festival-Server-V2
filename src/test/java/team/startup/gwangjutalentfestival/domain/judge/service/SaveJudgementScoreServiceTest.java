package team.startup.gwangjutalentfestival.domain.judge.service;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.startup.gwangjutalentfestival.domain.judge.entity.JudgementEntity;
import team.startup.gwangjutalentfestival.domain.judge.exception.JudgementTotalScoreExceededException;
import team.startup.gwangjutalentfestival.domain.judge.presentation.data.request.SaveJudgementScoreRequest;
import team.startup.gwangjutalentfestival.domain.judge.repository.JudgementRepository;
import team.startup.gwangjutalentfestival.domain.judge.service.impl.SaveJudgementScoreServiceImpl;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaveJudgementScoreServiceTest {

    private static final Long TEAM_ID = 1L;
    private static final Long NOT_FOUND_TEAM_ID = 99L;

    @Mock
    private JudgementRepository judgementRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private UserUtil userUtil;

    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

    private SaveJudgementScoreServiceImpl saveJudgementScoreService;

    private UserEntity user;
    private TeamEntity team;
    private SaveJudgementScoreRequest request;

    @BeforeEach
    void setUp() {
        saveJudgementScoreService = new SaveJudgementScoreServiceImpl(
                judgementRepository,
                teamRepository,
                userUtil,
                meterRegistry
        );

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
                .totalScore(0)
                .build();

        request = new SaveJudgementScoreRequest(10, 10, 10, 10, 10);
    }

    @Test
    void 심사_기록이_없으면_새로_저장된다() {
        given(userUtil.getCurrentUser()).willReturn(user);
        given(teamRepository.findById(TEAM_ID)).willReturn(Optional.of(team));
        given(judgementRepository.findByTeamAndUser(team, user)).willReturn(Optional.empty());
        given(judgementRepository.sumTotalScoreByTeam(team)).willReturn(50);

        saveJudgementScoreService.execute(request, TEAM_ID);

        verify(judgementRepository).save(any(JudgementEntity.class));
    }

    @Test
    void 심사_기록이_있으면_점수가_수정된다() {
        JudgementEntity existing = JudgementEntity.builder()
                .id(1L)
                .expressionCommunicationScore(5)
                .technicalCompletenessScore(5)
                .creativityCompositionScore(5)
                .stagePresencePerformanceScore(5)
                .teamworkStageHarmonyScore(5)
                .team(team)
                .user(user)
                .build();

        given(userUtil.getCurrentUser()).willReturn(user);
        given(teamRepository.findById(TEAM_ID)).willReturn(Optional.of(team));
        given(judgementRepository.findByTeamAndUser(team, user)).willReturn(Optional.of(existing));
        given(judgementRepository.sumTotalScoreByTeam(team)).willReturn(50);

        saveJudgementScoreService.execute(request, TEAM_ID);

        assertThat(existing.getExpressionCommunicationScore()).isEqualTo(10);
        assertThat(existing.getTechnicalCompletenessScore()).isEqualTo(10);
        assertThat(existing.getCreativityCompositionScore()).isEqualTo(10);
        assertThat(existing.getStagePresencePerformanceScore()).isEqualTo(10);
        assertThat(existing.getTeamworkStageHarmonyScore()).isEqualTo(10);
        verify(judgementRepository, never()).save(any());
    }

    @Test
    void 팀_총점이_100_초과하면_JudgementTotalScoreExceededException이_발생한다() {
        given(userUtil.getCurrentUser()).willReturn(user);
        given(teamRepository.findById(TEAM_ID)).willReturn(Optional.of(team));
        given(judgementRepository.findByTeamAndUser(team, user)).willReturn(Optional.empty());
        given(judgementRepository.sumTotalScoreByTeam(team)).willReturn(101);

        assertThatThrownBy(() -> saveJudgementScoreService.execute(request, TEAM_ID))
                .isInstanceOf(JudgementTotalScoreExceededException.class);
    }

    @Test
    void sumTotalScoreByTeam이_null을_반환하면_totalScore가_0으로_설정된다() {
        given(userUtil.getCurrentUser()).willReturn(user);
        given(teamRepository.findById(TEAM_ID)).willReturn(Optional.of(team));
        given(judgementRepository.findByTeamAndUser(team, user)).willReturn(Optional.empty());
        given(judgementRepository.sumTotalScoreByTeam(team)).willReturn(null);

        saveJudgementScoreService.execute(request, TEAM_ID);

        assertThat(team.getTotalScore()).isEqualTo(0);
    }

    @Test
    void 존재하지_않는_팀ID이면_TeamNotFoundException이_발생한다() {
        given(userUtil.getCurrentUser()).willReturn(user);
        given(teamRepository.findById(NOT_FOUND_TEAM_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> saveJudgementScoreService.execute(request, NOT_FOUND_TEAM_ID))
                .isInstanceOf(TeamNotFoundException.class);
    }
}
