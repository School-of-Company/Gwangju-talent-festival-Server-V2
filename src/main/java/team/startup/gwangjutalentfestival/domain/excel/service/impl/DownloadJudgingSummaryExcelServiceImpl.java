package team.startup.gwangjutalentfestival.domain.excel.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import team.startup.gwangjutalentfestival.domain.excel.service.DownloadJudgingSummaryExcelService;
import team.startup.gwangjutalentfestival.domain.judge.entity.JudgementEntity;
import team.startup.gwangjutalentfestival.domain.judge.repository.JudgementRepository;
import team.startup.gwangjutalentfestival.domain.team.entity.TeamEntity;
import team.startup.gwangjutalentfestival.domain.team.repository.TeamRepository;
import team.startup.gwangjutalentfestival.global.thirdparty.google.adapter.GoogleExcelAdapter;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DownloadJudgingSummaryExcelServiceImpl implements DownloadJudgingSummaryExcelService {

    private static final int MAX_JUDGE_COUNT = 6;

    private final TeamRepository teamRepository;
    private final JudgementRepository judgementRepository;
    private final GoogleExcelAdapter googleExcelAdapter;

    @Override
    public byte[] execute() {
        List<TeamEntity> teams = teamRepository.findAllByOrderByPerformOrderAsc();
        List<JudgementEntity> judgements = judgementRepository.findAllWithUserAndTeam();

        long totalJudgeCount = judgements.stream().map(j -> j.getUser().getId()).distinct().count();
        if (totalJudgeCount > MAX_JUDGE_COUNT) {
            log.warn("심사위원 수가 최대 허용 인원을 초과하였습니다. - total: {}, max: {}", totalJudgeCount, MAX_JUDGE_COUNT);
        }

        List<Long> judgeIds = judgements.stream()
                .map(j -> j.getUser().getId())
                .distinct()
                .sorted()
                .limit(MAX_JUDGE_COUNT)
                .toList();

        Map<Long, Map<Long, Integer>> scoreMap = buildScoreMap(judgements);
        Map<Long, Integer> teamTotalMap = buildTeamTotalMap(teams, judgeIds, scoreMap);
        Map<Long, Integer> rankMap = denseRank(teamTotalMap);

        List<List<Object>> rows = teams.stream()
                .map(t -> buildRow(t, judgeIds, scoreMap, teamTotalMap, rankMap))
                .toList();

        return googleExcelAdapter.exportSummary(rows);
    }

    private Map<Long, Map<Long, Integer>> buildScoreMap(List<JudgementEntity> judgements) {
        Map<Long, Map<Long, Integer>> scoreMap = new HashMap<>();
        for (JudgementEntity j : judgements) {
            int score = nz(j.getCompletenessExpressionScore())
                    + nz(j.getCreativityCompositionScore())
                    + nz(j.getStagePerformanceTeamworkScore());
            scoreMap.computeIfAbsent(j.getTeam().getId(), k -> new HashMap<>())
                    .put(j.getUser().getId(), score);
        }
        return scoreMap;
    }

    private Map<Long, Integer> buildTeamTotalMap(
            List<TeamEntity> teams,
            List<Long> judgeIds,
            Map<Long, Map<Long, Integer>> scoreMap) {
        return teams.stream()
                .collect(Collectors.toMap(
                        TeamEntity::getId,
                        t -> {
                            List<Integer> scores = judgeIds.stream()
                                    .map(judgeId -> scoreMap.getOrDefault(t.getId(), Collections.emptyMap()).get(judgeId))
                                    .toList();
                            return trimmedSum(scores);
                        }
                ));
    }

    private List<Object> buildRow(
            TeamEntity team,
            List<Long> judgeIds,
            Map<Long, Map<Long, Integer>> scoreMap,
            Map<Long, Integer> teamTotalMap,
            Map<Long, Integer> rankMap) {
        List<Object> row = new ArrayList<>();
        row.add(team.getPerformOrder());
        row.add(team.getTeamName());
        judgeIds.forEach(judgeId ->
                row.add(scoreMap.getOrDefault(team.getId(), Collections.emptyMap()).get(judgeId)));
        row.addAll(Collections.nCopies(MAX_JUDGE_COUNT - judgeIds.size(), null));
        row.add(teamTotalMap.get(team.getId()));
        row.add(rankMap.get(team.getId()));
        return row;
    }

    private int nz(Integer v) {
        return Optional.ofNullable(v).orElse(0);
    }

    private int trimmedSum(List<Integer> scores) {
        List<Integer> valid = scores.stream().filter(Objects::nonNull).toList();
        if (valid.isEmpty()) return 0;
        if (valid.size() < 3) return valid.stream().mapToInt(Integer::intValue).sum();
        return valid.stream()
                .sorted()
                .skip(1)
                .limit(valid.size() - 2)
                .mapToInt(Integer::intValue)
                .sum();
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