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

    private TeamEntity team(long id, int order, String name) {
        return TeamEntity.builder()
                .id(id)
                .teamName(name)
                .school("광주고")
                .teamStatus(TeamStatus.PENDING)
                .teamGenre(TeamGenre.SING)
                .performOrder(order)
                .totalScore(0)
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
    void 팀_목록이_없으면_빈_rows로_export가_호출된다() {
        given(teamRepository.findAllByOrderByPerformOrderAsc()).willReturn(List.of());
        given(judgementRepository.findAllWithUserAndTeam()).willReturn(List.of());

        service.execute();

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(googleExcelAdapter).exportSummary(captor.capture());
        assertThat(captor.getValue()).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void 심사위원이_없으면_총점이_0이고_1위가_된다() {
        TeamEntity teamA = team(1L, 1, "팀A");
        given(teamRepository.findAllByOrderByPerformOrderAsc()).willReturn(List.of(teamA));
        given(judgementRepository.findAllWithUserAndTeam()).willReturn(List.of());

        service.execute();

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(googleExcelAdapter).exportSummary(captor.capture());
        List<List<Object>> rows = captor.getValue();

        assertThat(rows).hasSize(1);
        List<Object> row = rows.get(0);
        assertThat(row.get(0)).isEqualTo(1);       // performOrder
        assertThat(row.get(1)).isEqualTo("팀A");   // teamName
        assertThat(row.get(8)).isEqualTo(0);        // total
        assertThat(row.get(9)).isEqualTo(1);        // rank
    }

    @Test
    @SuppressWarnings("unchecked")
    void 심사위원_2명_이하이면_전체_합산된다() {
        TeamEntity teamA = team(1L, 1, "팀A");
        UserEntity judge1 = user(1L);
        UserEntity judge2 = user(2L);
        // judge1: 10*3=30, judge2: 5*3=15 → total=45 (trimming 없음)
        given(teamRepository.findAllByOrderByPerformOrderAsc()).willReturn(List.of(teamA));
        given(judgementRepository.findAllWithUserAndTeam()).willReturn(List.of(
                judgement(teamA, judge1, 10, 10, 10),
                judgement(teamA, judge2, 5, 5, 5)
        ));

        service.execute();

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(googleExcelAdapter).exportSummary(captor.capture());
        List<Object> row = ((List<List<Object>>) captor.getValue()).get(0);

        assertThat(row.get(8)).isEqualTo(45);
    }

    @Test
    @SuppressWarnings("unchecked")
    void 심사위원_3명_이상이면_최고_최저_제거_후_합산된다() {
        TeamEntity teamA = team(1L, 1, "팀A");
        UserEntity judge1 = user(1L);
        UserEntity judge2 = user(2L);
        UserEntity judge3 = user(3L);
        // judge1=30, judge2=15, judge3=6 → 정렬: [6, 15, 30] → skip min/max → sum=15
        given(teamRepository.findAllByOrderByPerformOrderAsc()).willReturn(List.of(teamA));
        given(judgementRepository.findAllWithUserAndTeam()).willReturn(List.of(
                judgement(teamA, judge1, 10, 10, 10),
                judgement(teamA, judge2, 5, 5, 5),
                judgement(teamA, judge3, 2, 2, 2)
        ));

        service.execute();

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(googleExcelAdapter).exportSummary(captor.capture());
        List<Object> row = ((List<List<Object>>) captor.getValue()).get(0);

        assertThat(row.get(8)).isEqualTo(15);
    }

    @Test
    @SuppressWarnings("unchecked")
    void 동점_팀은_동일_순위가_부여된다() {
        TeamEntity teamA = team(1L, 1, "팀A");
        TeamEntity teamB = team(2L, 2, "팀B");
        TeamEntity teamC = team(3L, 3, "팀C");
        UserEntity judge1 = user(1L);
        // teamA=60, teamB=60, teamC=30 → A:1위, B:1위, C:2위
        given(teamRepository.findAllByOrderByPerformOrderAsc()).willReturn(List.of(teamA, teamB, teamC));
        given(judgementRepository.findAllWithUserAndTeam()).willReturn(List.of(
                judgement(teamA, judge1, 20, 20, 20),
                judgement(teamB, judge1, 20, 20, 20),
                judgement(teamC, judge1, 10, 10, 10)
        ));

        service.execute();

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(googleExcelAdapter).exportSummary(captor.capture());
        List<List<Object>> rows = (List<List<Object>>) captor.getValue();

        assertThat(rows.get(0).get(9)).isEqualTo(1);
        assertThat(rows.get(1).get(9)).isEqualTo(1);
        assertThat(rows.get(2).get(9)).isEqualTo(2);
    }

    @Test
    @SuppressWarnings("unchecked")
    void 심사위원_수가_6명_초과이면_6명까지만_집계한다() {
        TeamEntity teamA = team(1L, 1, "팀A");
        // judge ID 1~7 → limit(6) 적용 시 ID 1~6만 집계
        List<JudgementEntity> judgements = java.util.stream.LongStream.rangeClosed(1, 7)
                .mapToObj(id -> judgement(teamA, user(id), 10, 10, 10))
                .toList();
        given(teamRepository.findAllByOrderByPerformOrderAsc()).willReturn(List.of(teamA));
        given(judgementRepository.findAllWithUserAndTeam()).willReturn(judgements);

        service.execute();

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(googleExcelAdapter).exportSummary(captor.capture());
        List<List<Object>> rows = (List<List<Object>>) captor.getValue();
        // 심사위원 6명 모두 30점 → trimming 후: [30,30,30,30] → 합=120
        assertThat(rows.get(0).get(8)).isEqualTo(120);
    }
}