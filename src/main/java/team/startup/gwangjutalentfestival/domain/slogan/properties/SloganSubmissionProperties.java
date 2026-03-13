package team.startup.gwangjutalentfestival.domain.slogan.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.LocalDateTime;

@ConfigurationProperties(prefix = "slogan.submission")
public record SloganSubmissionProperties(
        LocalDateTime startAt,
        LocalDateTime endAt
) {
}
