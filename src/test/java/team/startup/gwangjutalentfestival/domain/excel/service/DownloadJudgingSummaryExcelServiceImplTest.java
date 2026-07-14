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
import team.startup.gwangjutalentfestival.global.thirdparty.google.adapter.GoogleExcelAdapter;

import java.util.List;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DownloadJudgingSummaryExcelServiceImplTest {

    @Mock private TeamRepository teamRepository;
    @Mock private JudgementRepository judgementRepository;
    @Mock private GoogleExcelAdapter googleExcelAdapter;

    private DownloadJudgingSummaryExcelServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DownloadJudgingSummaryExcelServiceImpl(teamRepository, judgementRepository, googleExcelAdapter);
        given(googleExcelAdapter.exportSummary(any())).willReturn(new byte[0]);
    }

    private TeamEntity team(long id, int order, String name, int totalScore) {
        return TeamEntity.builder()
                .id(id)
                .teamName(name)
                .school("광주고")
                .teamStatus(TeamStatus.PENDING)
                .teamGenre(TeamGenre.SING)
                .performOrder(order)
                .totalScore(totalScore)
                .build();
    }

    private UserEntity user(long id) {
        return UserEntity.builder().id(id).role(Role.ADMIN).build();
    }

    private JudgementEntity judgement(TeamEntity team, UserEntity user, int s1, int s2, int s3) {
        return JudgementEntity.builder()
                .completenessExpressionScore(s1)
                .creativityCompositionScore(s2)
                .stagePerformanceTeamworkScore(s3)
                .team(team)
                .user(user)
                .build();
    }

    @Test
    @SuppressWarnings("unchecked")
    void 팀_목록이_없으면_헤더행만_export된다() {
        given(teamRepository.findAllByOrderByPerformOrderAsc()).willReturn(List.of());
        given(judgementRepository.findAllWithUserAndTeam()).willReturn(List.of());

        service.execute();

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(googleExcelAdapter).exportSummary(captor.capture());
        List<List<Object>> rows = (List<List<Object>>) captor.getValue();

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0)).containsExactly("심사번호", "산출점수", "순위");
    }

    @Test
    @SuppressWarnings("unchecked")
    void 헤더행에_팀명_컬럼_없이_심사위원_수만큼_헤더가_생성된다() {
        TeamEntity teamA = team(1L, 1, "팀A", 0);
        UserEntity judge1 = user(1L);
        UserEntity judge2 = user(2L);
        given(teamRepository.findAllByOrderByPerformOrderAsc()).willReturn(List.of(teamA));
        given(judgementRepository.findAllWithUserAndTeam()).willReturn(List.of(
                judgement(teamA, judge1, 10, 10, 10),
                judgement(teamA, judge2, 5, 5, 5)
        ));

        service.execute();

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(googleExcelAdapter).exportSummary(captor.capture());
        List<List<Object>> rows = (List<List<Object>>) captor.getValue();

        assertThat(rows.get(0)).containsExactly("심사번호", "심사위원 (A)", "심사위원 (B)", "산출점수", "순위");
    }

    @Test
    @SuppressWarnings("unchecked")
    void 팀_데이터_행은_팀명_없이_심사번호_심사위원점수_산출점수_순위_순으로_구성된다() {
        TeamEntity teamA = team(1L, 1, "팀A", 45);
        UserEntity judge1 = user(1L);
        UserEntity judge2 = user(2L);
        given(teamRepository.findAllByOrderByPerformOrderAsc()).willReturn(List.of(teamA));
        given(judgementRepository.findAllWithUserAndTeam()).willReturn(List.of(
                judgement(teamA, judge1, 10, 10, 10),
                judgement(teamA, judge2, 5, 5, 5)
        ));

        service.execute();

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(googleExcelAdapter).exportSummary(captor.capture());
        List<List<Object>> rows = (List<List<Object>>) captor.getValue();
        List<Object> dataRow = rows.get(1);

        assertThat(dataRow).containsExactly(1, 30, 15, 45, 1);
    }

    @Test
    @SuppressWarnings("unchecked")
    void 산출점수는_team_totalScore를_그대로_재사용한다() {
        TeamEntity teamA = team(1L, 1, "팀A", 80);
        given(teamRepository.findAllByOrderByPerformOrderAsc()).willReturn(List.of(teamA));
        given(judgementRepository.findAllWithUserAndTeam()).willReturn(List.of());

        service.execute();

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(googleExcelAdapter).exportSummary(captor.capture());
        List<Object> dataRow = ((List<List<Object>>) captor.getValue()).get(1);

        assertThat(dataRow).containsExactly(1, 80, 1);
    }

    @Test
    @SuppressWarnings("unchecked")
    void 동점_팀은_동일_순위가_부여된다() {
        TeamEntity teamA = team(1L, 1, "팀A", 60);
        TeamEntity teamB = team(2L, 2, "팀B", 60);
        TeamEntity teamC = team(3L, 3, "팀C", 30);
        given(teamRepository.findAllByOrderByPerformOrderAsc()).willReturn(List.of(teamA, teamB, teamC));
        given(judgementRepository.findAllWithUserAndTeam()).willReturn(List.of());

        service.execute();

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(googleExcelAdapter).exportSummary(captor.capture());
        List<List<Object>> rows = (List<List<Object>>) captor.getValue();

        assertThat(rows.get(1).get(2)).isEqualTo(1); // teamA 순위
        assertThat(rows.get(2).get(2)).isEqualTo(1); // teamB 순위
        assertThat(rows.get(3).get(2)).isEqualTo(2); // teamC 순위
    }

    @Test
    @SuppressWarnings("unchecked")
    void 심사위원_수가_안전_상한을_초과하면_상한까지만_집계한다() {
        TeamEntity teamA = team(1L, 1, "팀A", 0);
        // judge ID 1~21 → 안전 상한(20) 적용 시 ID 1~20만 헤더/컬럼에 반영
        List<JudgementEntity> judgements = LongStream.rangeClosed(1, 21)
                .mapToObj(id -> judgement(teamA, user(id), 10, 10, 10))
                .toList();
        given(teamRepository.findAllByOrderByPerformOrderAsc()).willReturn(List.of(teamA));
        given(judgementRepository.findAllWithUserAndTeam()).willReturn(judgements);

        service.execute();

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(googleExcelAdapter).exportSummary(captor.capture());
        List<List<Object>> rows = (List<List<Object>>) captor.getValue();

        // 헤더: 심사번호 + 심사위원 20명 + 산출점수 + 순위 = 23컬럼
        assertThat(rows.get(0)).hasSize(23);
    }
}
