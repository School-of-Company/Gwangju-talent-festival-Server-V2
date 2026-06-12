package team.startup.gwangjutalentfestival.global.thirdparty.google.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "google.excel")
public record GoogleExcelProperties(
        String templateSheetId,
        String summaryPage
) {
}
