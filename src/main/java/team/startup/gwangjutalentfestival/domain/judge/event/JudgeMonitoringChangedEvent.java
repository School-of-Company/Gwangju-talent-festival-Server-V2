package team.startup.gwangjutalentfestival.domain.judge.event;

public record JudgeMonitoringChangedEvent(Type type, Long teamId, Long judgeId) {

    public static JudgeMonitoringChangedEvent scoreChanged() {
        return new JudgeMonitoringChangedEvent(Type.SCORE, null, null);
    }

    public static JudgeMonitoringChangedEvent commentChanged(Long teamId, Long judgeId) {
        return new JudgeMonitoringChangedEvent(Type.COMMENT, teamId, judgeId);
    }

    public enum Type {
        SCORE, COMMENT
    }
}
