package team.startup.gwangjutalentfestival.domain.monitoring.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("monitoring.request-alert")
public record RequestAlertProperties(
        boolean enabled,
        long slowThresholdMillis
) {}
