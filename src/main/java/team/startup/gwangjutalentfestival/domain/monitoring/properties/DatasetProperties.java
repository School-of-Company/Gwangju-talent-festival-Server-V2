package team.startup.gwangjutalentfestival.domain.monitoring.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("monitoring.dataset")
public record DatasetProperties(String exportPath) {}