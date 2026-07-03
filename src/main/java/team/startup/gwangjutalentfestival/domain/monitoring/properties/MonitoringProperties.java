package team.startup.gwangjutalentfestival.domain.monitoring.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("monitoring.anomaly")
public record MonitoringProperties(
        boolean enabled,
        String prometheusBaseUrl,
        String discordWebhookUrl
) {}
