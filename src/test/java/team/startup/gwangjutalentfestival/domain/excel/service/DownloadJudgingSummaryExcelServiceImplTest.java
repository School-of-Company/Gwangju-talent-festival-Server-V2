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
        assertThat(rows.get(0)).containsExactly("심사번호", "팀명", "심사위원 (A)", "심사위원 (B)", "산출점수", "순위");
        assertThat(rows.get(1)).containsExactly(1, "팀A", 60, 0, 60.0, 1);
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
        assertThat(((List<List<Object>>) captor.getValue()).get(1)).containsExactly(1, "팀A", 60, 60.0, 1);
    }

    @Test
    @SuppressWarnings("unchecked")
    void 산출점수가_같으면_완성도_최고최저_제외평균이_높은_팀을_먼저_순위로_매긴다() {
        TeamEntity teamA = team(2L, 1, "팀A");
        TeamEntity teamB = team(1L, 2, "팀B");
        List<UserEntity> judges = java.util.stream.LongStream.rangeClosed(1, 4)
                .mapToObj(id -> user(id, Role.JUDGE))
                .toList();
        given(teamRepository.findAllByOrderByPerformOrderAsc()).willReturn(List.of(teamA, teamB));
        given(userRepository.findAllByRoleOrderByIdAsc(Role.JUDGE)).willReturn(judges);
        given(judgementRepository.findAllWithUserAndTeam()).willReturn(List.of(
                judgement(teamA, judges.get(0), 0, 30, 30),
                judgement(teamA, judges.get(1), 20, 20, 20),
                judgement(teamA, judges.get(2), 20, 20, 20),
                judgement(teamA, judges.get(3), 20, 20, 20),
                judgement(teamB, judges.get(0), 18, 21, 21),
                judgement(teamB, judges.get(1), 18, 21, 21),
                judgement(teamB, judges.get(2), 18, 21, 21),
                judgement(teamB, judges.get(3), 30, 15, 15)
        ));

        service.execute();

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(googleExcelAdapter).exportSummary(captor.capture());
        List<List<Object>> rows = captor.getValue();
        assertThat(rows.get(1)).containsExactly(1, "팀A", 60, 60, 60, 60, 60.0, 1);
        assertThat(rows.get(2)).containsExactly(2, "팀B", 60, 60, 60, 60, 60.0, 2);
    }

    @Test
    @SuppressWarnings("unchecked")
    void 모든_동점_기준이_같으면_팀_ID_오름차순으로_고유_순위를_매긴다() {
        TeamEntity teamA = team(2L, 1, "팀A");
        TeamEntity teamB = team(1L, 2, "팀B");
        UserEntity judge = user(1L, Role.JUDGE);
        given(teamRepository.findAllByOrderByPerformOrderAsc()).willReturn(List.of(teamA, teamB));
        given(userRepository.findAllByRoleOrderByIdAsc(Role.JUDGE)).willReturn(List.of(judge));
        given(judgementRepository.findAllWithUserAndTeam()).willReturn(List.of(
                judgement(teamA, judge, 30, 20, 20),
                judgement(teamB, judge, 30, 20, 20)
        ));

        service.execute();

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(googleExcelAdapter).exportSummary(captor.capture());
        List<List<Object>> rows = captor.getValue();
        assertThat(rows.get(1)).containsExactly(1, "팀A", 70, 70.0, 2);
        assertThat(rows.get(2)).containsExactly(2, "팀B", 70, 70.0, 1);
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
