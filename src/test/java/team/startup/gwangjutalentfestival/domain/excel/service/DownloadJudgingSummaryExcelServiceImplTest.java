package team.startup.gwangjutalentfestival.domain.excel.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.startup.gwangjutalentfestival.domain.excel.service.impl.DownloadJudgingSummaryExcelServiceImpl;
import team.startup.gwangjutalentfestival.domain.judge.entity.JudgementEntity;
import team.startup.gwangjutalentfestival.domain.judge.repository.JudgementRepository;
import team.startup.gwangjutalentfestival.domain.team.entity.TeamEntity;
import team.startup.gwangjutalentfestival.domain.team.enums.TeamGenre;
import team.startup.gwangjutalentfestival.domain.team.enums.TeamStatus;
import team.startup.gwangjutalentfestival.domain.team.repository.TeamRepository;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;
import team.startup.gwangjutalentfestival.domain.user.repository.UserRepository;
import team.startup.gwangjutalentfestival.global.thirdparty.google.adapter.GoogleExcelAdapter;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DownloadJudgingSummaryExcelServiceImplTest {

    @Mock private TeamRepository teamRepository;
    @Mock private JudgementRepository judgementRepository;
    @Mock private UserRepository userRepository;
    @Mock private GoogleExcelAdapter googleExcelAdapter;

    private DownloadJudgingSummaryExcelServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DownloadJudgingSummaryExcelServiceImpl(teamRepository, judgementRepository, userRepository, googleExcelAdapter);
        given(googleExcelAdapter.exportSummary(any())).willReturn(new byte[0]);
    }

    @Test
    @SuppressWarnings("unchecked")
    void JUDGE_전체를_심사위원_열로_포함하고_팀명을_출력한다() {
        TeamEntity team = team(1L, 1, "팀A");
        UserEntity judgeA = user(1L, Role.JUDGE);
        UserEntity judgeB = user(2L, Role.JUDGE);
        given(teamRepository.findAllByOrderByPerformOrderAsc()).willReturn(List.of(team));
        given(userRepository.findAllByRoleOrderByIdAsc(Role.JUDGE)).willReturn(List.of(judgeA, judgeB));
        given(judgementRepository.findAllWithUserAndTeam()).willReturn(List.of(judgement(team, judgeA, 20, 20, 20)));

        service.execute();

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(googleExcelAdapter).exportSummary(captor.capture());
        List<List<Object>> rows = captor.getValue();
        assertThat(rows.get(0)).containsExactly("심사순서", "팀명", "심사위원 (A)", "심사위원 (B)", "산출점수", "순위");
        assertThat(rows.get(1)).containsExactly(1, "팀A", 60, 0, 60, 1);
    }

    @Test
    @SuppressWarnings("unchecked")
    void ADMIN의_기존_점수는_집계에서_제외한다() {
        TeamEntity team = team(1L, 1, "팀A");
        UserEntity judge = user(1L, Role.JUDGE);
        UserEntity admin = user(2L, Role.ADMIN);
        given(teamRepository.findAllByOrderByPerformOrderAsc()).willReturn(List.of(team));
        given(userRepository.findAllByRoleOrderByIdAsc(Role.JUDGE)).willReturn(List.of(judge));
        given(judgementRepository.findAllWithUserAndTeam()).willReturn(List.of(
                judgement(team, judge, 20, 20, 20),
                judgement(team, admin, 1, 1, 1)
        ));

        service.execute();

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(googleExcelAdapter).exportSummary(captor.capture());
        assertThat(((List<List<Object>>) captor.getValue()).get(1)).containsExactly(1, "팀A", 60, 60, 1);
    }

    private TeamEntity team(long id, int order, String name) {
        return TeamEntity.builder().id(id).teamName(name).school("광주고")
                .teamStatus(TeamStatus.PENDING).teamGenre(TeamGenre.SING).performOrder(order).totalScore(0).build();
    }

    private UserEntity user(long id, Role role) {
        return UserEntity.builder().id(id).role(role).build();
    }

    private JudgementEntity judgement(TeamEntity team, UserEntity user, int first, int second, int third) {
        return JudgementEntity.builder().team(team).user(user).completenessExpressionScore(first)
                .creativityCompositionScore(second).stagePerformanceTeamworkScore(third).build();
    }
}
