package team.startup.gwangjutalentfestival.domain.monitoring.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import team.startup.gwangjutalentfestival.domain.monitoring.properties.MonitoringProperties;

import java.util.Map;

@Slf4j
@Component
public class DiscordWebhookClient {

    private final RestClient discordRestClient;
    private final MonitoringProperties monitoringProperties;

    public DiscordWebhookClient(
            @Qualifier("discordRestClient") RestClient discordRestClient,
            MonitoringProperties monitoringProperties
    ) {
        this.discordRestClient = discordRestClient;
        this.monitoringProperties = monitoringProperties;
    }

    public void send(String message) {
        String webhookUrl = monitoringProperties.discordWebhookUrl();
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.warn("Discord webhook URL이 설정되지 않아 알림을 전송하지 않습니다.");
            return;
        }

        try {
            discordRestClient.post()
                    .uri(webhookUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("content", message))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Discord 알림 전송 실패. error={}", e.getMessage());
        }
    }
}
