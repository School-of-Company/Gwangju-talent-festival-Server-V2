package team.startup.gwangjutalentfestival.domain.monitoring.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "monitoring.ml")
public record MlProperties(boolean enabled, String baseUrl, long timeoutMs) {

    private static final long DEFAULT_TIMEOUT_MS = 3000L;

    public MlProperties {
        if (baseUrl == null) {
            baseUrl = "";
        }
        if (timeoutMs <= 0) {
            timeoutMs = DEFAULT_TIMEOUT_MS;
        }
    }
}
