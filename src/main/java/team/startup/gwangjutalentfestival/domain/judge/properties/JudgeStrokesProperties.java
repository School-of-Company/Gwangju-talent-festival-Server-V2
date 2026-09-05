package team.startup.gwangjutalentfestival.domain.judge.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("judge.strokes")
public record JudgeStrokesProperties(
        int maxBytes
) {}
