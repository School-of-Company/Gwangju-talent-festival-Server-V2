package team.startup.gwangjutalentfestival.domain.team.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import team.startup.gwangjutalentfestival.domain.judge.event.JudgementTeamEvent;
import team.startup.gwangjutalentfestival.domain.team.entity.TeamEntity;
import team.startup.gwangjutalentfestival.domain.team.enums.TeamGenre;
import team.startup.gwangjutalentfestival.domain.team.enums.TeamStatus;
import team.startup.gwangjutalentfestival.domain.team.exception.TeamAlreadyFinishedException;
import team.startup.gwangjutalentfestival.domain.team.exception.TeamAlreadyOngoingException;
import team.startup.gwangjutalentfestival.domain.team.exception.TeamNotFoundException;
import team.startup.gwangjutalentfestival.domain.team.repository.TeamRepository;
import team.startup.gwangjutalentfestival.domain.team.service.impl.UpdateTeamPerformanceServiceImpl;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UpdateTeamPerformanceServiceTest {

    private static final Long PENDING_TEAM_ID = 1L;
    private static final Long ONGOING_TEAM_ID = 2L;
    private static final Long FINISHED_TEAM_ID = 3L;
    private static final Long NOT_FOUND_TEAM_ID = 99L;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private UpdateTeamPerformanceServiceImpl updateTeamPerformanceService;

    private TeamEntity pendingTeam;
    private TeamEntity ongoingTeam;
    private TeamEntity finishedTeam;

    @BeforeEach
    void setUp() {
        pendingTeam = TeamEntity.builder()
                .id(PENDING_TEAM_ID)
                .teamName("대기팀")
                .school("광주고")
                .teamStatus(TeamStatus.PENDING)
                .teamGenre(TeamGenre.SING)
                .performOrder(1)
                .totalScore(0)
                .build();

        ongoingTeam = TeamEntity.builder()
                .id(ONGOING_TEAM_ID)
                .teamName("진행팀")
                .school("광주고")
                .teamStatus(TeamStatus.ONGOING)
                .teamGenre(TeamGenre.SING)
                .performOrder(2)
                .totalScore(0)
                .build();

        finishedTeam = TeamEntity.builder()
                .id(FINISHED_TEAM_ID)
                .teamName("종료팀")
                .school("광주고")
                .teamStatus(TeamStatus.FINISHED)
                .teamGenre(TeamGenre.SING)
                .performOrder(3)
                .totalScore(0)
                .build();
    }

    @Test
    void PENDING_상태의_팀은_ONGOING으로_변경되고_이벤트가_발행된다() {
        given(teamRepository.findById(PENDING_TEAM_ID)).willReturn(Optional.of(pendingTeam));

        updateTeamPerformanceService.execute(PENDING_TEAM_ID);

        assertThat(pendingTeam.getTeamStatus()).isEqualTo(TeamStatus.ONGOING);

        ArgumentCaptor<JudgementTeamEvent> captor = ArgumentCaptor.forClass(JudgementTeamEvent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().getTeamId()).isEqualTo(PENDING_TEAM_ID);
    }

    @Test
    void 존재하지_않는_팀이면_TeamNotFoundException이_발생한다() {
        given(teamRepository.findById(NOT_FOUND_TEAM_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> updateTeamPerformanceService.execute(NOT_FOUND_TEAM_ID))
                .isInstanceOf(TeamNotFoundException.class);
    }

    @Test
    void 이미_종료된_팀이면_TeamAlreadyFinishedException이_발생한다() {
        given(teamRepository.findById(FINISHED_TEAM_ID)).willReturn(Optional.of(finishedTeam));

        assertThatThrownBy(() -> updateTeamPerformanceService.execute(FINISHED_TEAM_ID))
                .isInstanceOf(TeamAlreadyFinishedException.class);
    }
}
