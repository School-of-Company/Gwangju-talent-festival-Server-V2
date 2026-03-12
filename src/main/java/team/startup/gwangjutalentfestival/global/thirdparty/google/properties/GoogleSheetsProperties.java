package team.startup.gwangjutalentfestival.global.thirdparty.google.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "google.sheets")
public record GoogleSheetsProperties(
        String accountCredential,
        String sheetId,
        String sheetPage
) {
}
