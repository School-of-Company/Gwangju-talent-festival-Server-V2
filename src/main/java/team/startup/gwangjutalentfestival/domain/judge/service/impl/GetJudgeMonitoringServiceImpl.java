package team.startup.gwangjutalentfestival.domain.judge.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.startup.gwangjutalentfestival.domain.judge.entity.JudgeCommentEntity;
import team.startup.gwangjutalentfestival.domain.judge.entity.JudgementEntity;
import team.startup.gwangjutalentfestival.domain.judge.presentation.data.response.JudgeMonitoringResponse;
import team.startup.gwangjutalentfestival.domain.judge.repository.JudgeCommentRepository;
import team.startup.gwangjutalentfestival.domain.judge.repository.JudgementRepository;
import team.startup.gwangjutalentfestival.domain.judge.service.GetJudgeMonitoringService;
import team.startup.gwangjutalentfestival.domain.judge.util.JudgeRankingCalculator;
import team.startup.gwangjutalentfestival.domain.judge.util.JudgeScoreCalculator;
import team.startup.gwangjutalentfestival.domain.team.entity.TeamEntity;
import team.startup.gwangjutalentfestival.domain.team.repository.TeamRepository;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;
import team.startup.gwangjutalentfestival.domain.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetJudgeMonitoringServiceImpl implements GetJudgeMonitoringService {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final JudgementRepository judgementRepository;
    private final JudgeCommentRepository judgeCommentRepository;

    @Override
    @Transactional(readOnly = true)
    public JudgeMonitoringResponse execute() {
        List<UserEntity> judges = userRepository.findAllByRoleOrderByIdAsc(Role.JUDGE);
        List<TeamEntity> teams = teamRepository.findAllByOrderByPerformOrderAsc();
        Set<Long> judgeIds = judges.stream().map(UserEntity::getId).collect(Collectors.toSet());
        Map<JudgeTeamKey, JudgementEntity> judgements = judgementRepository.findAllWithUserAndTeam().stream()
                .filter(judgement -> judgeIds.contains(judgement.getUser().getId()))
                .collect(Collectors.toMap(
                        judgement -> new JudgeTeamKey(judgement.getUser().getId(), judgement.getTeam().getId()),
                        judgement -> judgement
                ));
        Map<JudgeTeamKey, JudgeCommentEntity> comments = judgeCommentRepository.findAllWithUserAndTeam().stream()
                .filter(comment -> judgeIds.contains(comment.getUser().getId()))
                .collect(Collectors.toMap(
                        comment -> new JudgeTeamKey(comment.getUser().getId(), comment.getTeam().getId()),
                        comment -> comment
                ));

        Map<Long, Integer> calculatedScores = calculateScores(teams, judges, judgements);
        Map<Long, Integer> ranks = JudgeRankingCalculator.calculate(
                teams, judgements.values(), calculatedScores);
        List<JudgeMonitoringResponse.JudgeHeader> headers = headers(judges);

        return new JudgeMonitoringResponse(
                headers,
                scoreRows(teams, judges, judgements, calculatedScores, ranks),
                commentRows(teams, judges, comments)
        );
    }

    private List<JudgeMonitoringResponse.JudgeHeader> headers(List<UserEntity> judges) {
        List<JudgeMonitoringResponse.JudgeHeader> result = new ArrayList<>();
        for (int index = 0; index < judges.size(); index++) {
            result.add(new JudgeMonitoringResponse.JudgeHeader(judges.get(index).getId(), "심사위원 " + (char) ('A' + index)));
        }
        return result;
    }

    private Map<Long, Integer> calculateScores(
            List<TeamEntity> teams,
            List<UserEntity> judges,
            Map<JudgeTeamKey, JudgementEntity> judgements) {
        Map<Long, Integer> result = new HashMap<>();
        for (TeamEntity team : teams) {
            List<Integer> scores = judges.stream()
                    .map(judge -> judgements.get(new JudgeTeamKey(judge.getId(), team.getId())))
                    .filter(judgement -> judgement != null)
                    .map(this::total)
                    .toList();
            result.put(team.getId(), JudgeScoreCalculator.calculate(scores));
        }
        return result;
    }

    private List<JudgeMonitoringResponse.ScoreRow> scoreRows(
            List<TeamEntity> teams,
            List<UserEntity> judges,
            Map<JudgeTeamKey, JudgementEntity> judgements,
            Map<Long, Integer> calculatedScores,
            Map<Long, Integer> ranks) {
        return teams.stream().map(team -> new JudgeMonitoringResponse.ScoreRow(
                team.getId(), team.getPerformOrder(), team.getTeamName(),
                judges.stream().map(judge -> {
                    JudgementEntity judgement = judgements.get(new JudgeTeamKey(judge.getId(), team.getId()));
                    return new JudgeMonitoringResponse.ScoreCell(judge.getId(), judgement == null ? null : total(judgement));
                }).toList(),
                calculatedScores.get(team.getId()), ranks.get(team.getId())
        )).toList();
    }

    private List<JudgeMonitoringResponse.CommentRow> commentRows(
            List<TeamEntity> teams,
            List<UserEntity> judges,
            Map<JudgeTeamKey, JudgeCommentEntity> comments) {
        return teams.stream().map(team -> new JudgeMonitoringResponse.CommentRow(
                team.getId(), team.getPerformOrder(), team.getTeamName(),
                judges.stream().map(judge -> {
                    JudgeCommentEntity comment = comments.get(new JudgeTeamKey(judge.getId(), team.getId()));
                    return new JudgeMonitoringResponse.CommentCell(judge.getId(), comment == null ? null : comment.getStrokes());
                }).toList()
        )).toList();
    }

    private int total(JudgementEntity judgement) {
        return judgement.getCompletenessExpressionScore()
                + judgement.getCreativityCompositionScore()
                + judgement.getStagePerformanceTeamworkScore();
    }

    private record JudgeTeamKey(Long judgeId, Long teamId) {
    }
}
