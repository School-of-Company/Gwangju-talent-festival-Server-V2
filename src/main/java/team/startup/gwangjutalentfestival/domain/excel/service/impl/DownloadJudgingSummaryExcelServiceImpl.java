package team.startup.gwangjutalentfestival.domain.excel.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.excel.service.DownloadJudgingSummaryExcelService;
import team.startup.gwangjutalentfestival.domain.judge.entity.JudgementEntity;
import team.startup.gwangjutalentfestival.domain.judge.repository.JudgementRepository;
import team.startup.gwangjutalentfestival.domain.team.entity.TeamEntity;
import team.startup.gwangjutalentfestival.domain.team.repository.TeamRepository;
import team.startup.gwangjutalentfestival.global.thirdparty.google.adapter.GoogleExcelAdapter;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DownloadJudgingSummaryExcelServiceImpl implements DownloadJudgingSummaryExcelService {

    private final TeamRepository teamRepository;
    private final JudgementRepository judgementRepository;
    private final GoogleExcelAdapter googleExcelAdapter;

    @Override
    public byte[] execute() {
        List<TeamEntity> teams = teamRepository.findAllByOrderByPerformOrderAsc();
        List<JudgementEntity> judgements = judgementRepository.findAllWithUserAndTeam();

        List<Long> judgeIds = judgements.stream()
                .map(j -> j.getUser().getId())
                .distinct()
                .sorted()
                .limit(6)
                .toList();

        Map<Long, Map<Long, Integer>> scoreMap = new HashMap<>();
        for (JudgementEntity j : judgements) {
            Long teamId = j.getTeam().getId();
            Long userId = j.getUser().getId();
            int score = nz(j.getExpressionCommunicationScore())
                    + nz(j.getTechnicalCompletenessScore())
                    + nz(j.getCreativityCompositionScore())
                    + nz(j.getStagePresencePerformanceScore())
                    + nz(j.getTeamworkStageHarmonyScore());
            scoreMap.computeIfAbsent(teamId, k -> new HashMap<>()).put(userId, score);
        }

        Map<Long, Integer> teamTotalMap = teams.stream()
                .collect(Collectors.toMap(
                        TeamEntity::getId,
                        t -> {
                            List<Integer> scores = judgeIds.stream()
                                    .map(judgeId -> scoreMap.getOrDefault(t.getId(), Collections.emptyMap()).get(judgeId))
                                    .toList();
                            return trimmedSum(scores);
                        }
                ));

        Map<Long, Integer> rankMap = denseRank(teamTotalMap);

        List<List<Object>> rows = teams.stream()
                .map(t -> {
                    List<Object> row = new ArrayList<>();
                    row.add(t.getPerformOrder());
                    row.add(t.getTeamName());
                    for (Long judgeId : judgeIds) {
                        row.add(scoreMap.getOrDefault(t.getId(), Collections.emptyMap()).get(judgeId));
                    }
                    for (int i = judgeIds.size(); i < 6; i++) {
                        row.add(null);
                    }
                    row.add(teamTotalMap.get(t.getId()));
                    row.add(rankMap.get(t.getId()));
                    return row;
                })
                .toList();

        return googleExcelAdapter.exportSummary(rows);
    }

    private int nz(Integer v) {
        return v == null ? 0 : v;
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
