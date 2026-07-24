package team.startup.gwangjutalentfestival.domain.excel.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import team.startup.gwangjutalentfestival.domain.excel.service.DownloadJudgingSummaryExcelService;
import team.startup.gwangjutalentfestival.domain.judge.entity.JudgementEntity;
import team.startup.gwangjutalentfestival.domain.judge.repository.JudgementRepository;
import team.startup.gwangjutalentfestival.domain.team.entity.TeamEntity;
import team.startup.gwangjutalentfestival.domain.team.repository.TeamRepository;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;
import team.startup.gwangjutalentfestival.domain.user.repository.UserRepository;
import team.startup.gwangjutalentfestival.domain.judge.util.JudgeScoreCalculator;
import team.startup.gwangjutalentfestival.global.thirdparty.google.adapter.GoogleExcelAdapter;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DownloadJudgingSummaryExcelServiceImpl implements DownloadJudgingSummaryExcelService {

    // ponytail: 안전 상한. 알파벳 라벨(A~Z) 26개 범위 내에서 여유 있게 잡음. 초과 시 멀티레터 라벨링 필요.
    private static final int MAX_JUDGE_COLUMNS = 20;

    private final TeamRepository teamRepository;
    private final JudgementRepository judgementRepository;
    private final UserRepository userRepository;
    private final GoogleExcelAdapter googleExcelAdapter;

    @Override
    public byte[] execute() {
        List<TeamEntity> teams = teamRepository.findAllByOrderByPerformOrderAsc();
        List<JudgementEntity> judgements = judgementRepository.findAllWithUserAndTeam();
        List<Long> allJudgeIds = userRepository.findAllByRoleOrderByIdAsc(Role.JUDGE).stream()
                .map(UserEntity::getId)
                .toList();
        List<Long> judgeIds = allJudgeIds.stream()
                .limit(MAX_JUDGE_COLUMNS)
                .toList();

        long totalJudgeCount = allJudgeIds.size();
        if (totalJudgeCount > MAX_JUDGE_COLUMNS) {
            log.warn("심사위원 수가 최대 허용 인원을 초과하였습니다. - total: {}, max: {}", totalJudgeCount, MAX_JUDGE_COLUMNS);
        }

        Map<Long, Map<Long, Integer>> scoreMap = buildScoreMap(judgements, judgeIds);
        Map<Long, Integer> teamTotalMap = teams.stream().collect(Collectors.toMap(
                TeamEntity::getId,
                team -> JudgeScoreCalculator.calculate(scoreMap.getOrDefault(team.getId(), Collections.emptyMap()).values())
        ));
        Map<Long, Integer> rankMap = denseRank(teamTotalMap);

        List<List<Object>> rows = new ArrayList<>();
        rows.add(buildHeaderRow(judgeIds.size()));
        teams.forEach(t -> rows.add(buildRow(t, judgeIds, scoreMap, teamTotalMap, rankMap)));

        return googleExcelAdapter.exportSummary(rows);
    }

    private Map<Long, Map<Long, Integer>> buildScoreMap(List<JudgementEntity> judgements, List<Long> judgeIds) {
        Map<Long, Map<Long, Integer>> scoreMap = new HashMap<>();
        for (JudgementEntity j : judgements) {
            if (!judgeIds.contains(j.getUser().getId())) {
                continue;
            }
            int score = nz(j.getCompletenessExpressionScore())
                    + nz(j.getCreativityCompositionScore())
                    + nz(j.getStagePerformanceTeamworkScore());
            scoreMap.computeIfAbsent(j.getTeam().getId(), k -> new HashMap<>())
                    .put(j.getUser().getId(), score);
        }
        return scoreMap;
    }

    private List<Object> buildHeaderRow(int judgeCount) {
        List<Object> header = new ArrayList<>();
        header.add("심사순서");
        header.add("팀명");
        for (int i = 0; i < judgeCount; i++) {
            header.add("심사위원 (" + (char) ('A' + i) + ")");
        }
        header.add("산출점수");
        header.add("순위");
        return header;
    }

    private List<Object> buildRow(
            TeamEntity team,
            List<Long> judgeIds,
            Map<Long, Map<Long, Integer>> scoreMap,
            Map<Long, Integer> teamTotalMap,
            Map<Long, Integer> rankMap) {
        List<Object> row = new ArrayList<>();
        row.add(nz(team.getPerformOrder()));
        row.add(team.getTeamName());
        Map<Long, Integer> teamScores = scoreMap.getOrDefault(team.getId(), Collections.emptyMap());
        judgeIds.forEach(judgeId -> row.add(teamScores.getOrDefault(judgeId, 0)));
        row.add(nz(teamTotalMap.get(team.getId())));
        row.add(nz(rankMap.get(team.getId())));
        return row;
    }

    private int nz(Integer v) {
        return Optional.ofNullable(v).orElse(0);
    }

    private Map<Long, Integer> denseRank(Map<Long, Integer> teamTotalMap) {
        List<Integer> sortedScores = teamTotalMap.values().stream()
                .distinct()
                .sorted(Comparator.reverseOrder())
                .toList();
        Map<Integer, Integer> scoreToRank = new HashMap<>();
        for (int i = 0; i < sortedScores.size(); i++) {
            scoreToRank.put(sortedScores.get(i), i + 1);
        }
        return teamTotalMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> scoreToRank.get(e.getValue())));
    }
}
