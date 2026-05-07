package team.startup.gwangjutalentfestival.domain.team.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import team.startup.gwangjutalentfestival.domain.team.entity.TeamEntity;
import team.startup.gwangjutalentfestival.domain.team.enums.TeamGenre;
import team.startup.gwangjutalentfestival.domain.team.enums.TeamStatus;
import team.startup.gwangjutalentfestival.domain.team.event.TeamOrderChangedEvent;
import team.startup.gwangjutalentfestival.domain.team.exception.TeamNotFoundException;
import team.startup.gwangjutalentfestival.domain.team.presentation.data.TeamOrderItem;
import team.startup.gwangjutalentfestival.domain.team.repository.TeamRepository;
import team.startup.gwangjutalentfestival.domain.team.service.impl.UpdateTeamOrderServiceImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UpdateTeamOrderServiceTest {

    private static final Long TEAM_ID_1 = 1L;
    private static final Long TEAM_ID_2 = 2L;
    private static final Long NOT_FOUND_TEAM_ID = 99L;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private UpdateTeamOrderServiceImpl updateTeamOrderService;

    private TeamEntity team1;
    private TeamEntity team2;

    @BeforeEach
    void setUp() {
        team1 = TeamEntity.builder()
                .id(TEAM_ID_1)
                .teamName("팀A")
                .school("광주고")
                .teamStatus(TeamStatus.PENDING)
                .teamGenre(TeamGenre.SING)
                .performOrder(1)
                .totalScore(0)
                .build();

        team2 = TeamEntity.builder()
                .id(TEAM_ID_2)
                .teamName("팀B")
                .school("광주고")
                .teamStatus(TeamStatus.PENDING)
                .teamGenre(TeamGenre.SING)
                .performOrder(2)
                .totalScore(0)
                .build();
    }

    @Test
    void 팀_순서가_정상적으로_업데이트되고_이벤트가_발행된다() {
        List<TeamOrderItem> orders = List.of(
                new TeamOrderItem(TEAM_ID_1, 3),
                new TeamOrderItem(TEAM_ID_2, 4)
        );
        given(teamRepository.findAllByIdIn(List.of(TEAM_ID_1, TEAM_ID_2)))
                .willReturn(List.of(team1, team2));

        updateTeamOrderService.execute(orders);

        assertThat(team1.getPerformOrder()).isEqualTo(3);
        assertThat(team2.getPerformOrder()).isEqualTo(4);

        ArgumentCaptor<TeamOrderChangedEvent> captor = ArgumentCaptor.forClass(TeamOrderChangedEvent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().getOrders()).isEqualTo(orders);
    }

    @Test
    void 존재하지_않는_팀ID가_포함되면_TeamNotFoundException이_발생한다() {
        List<TeamOrderItem> orders = List.of(
                new TeamOrderItem(TEAM_ID_1, 1),
                new TeamOrderItem(NOT_FOUND_TEAM_ID, 2)
        );
        given(teamRepository.findAllByIdIn(List.of(TEAM_ID_1, NOT_FOUND_TEAM_ID)))
                .willReturn(List.of(team1));

        assertThatThrownBy(() -> updateTeamOrderService.execute(orders))
                .isInstanceOf(TeamNotFoundException.class);
    }

    @Test
    void 빈_리스트로_호출하면_이벤트만_발행된다() {
        given(teamRepository.findAllByIdIn(List.of())).willReturn(List.of());

        updateTeamOrderService.execute(List.of());

        ArgumentCaptor<TeamOrderChangedEvent> captor = ArgumentCaptor.forClass(TeamOrderChangedEvent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().getOrders()).isEmpty();
    }
}
