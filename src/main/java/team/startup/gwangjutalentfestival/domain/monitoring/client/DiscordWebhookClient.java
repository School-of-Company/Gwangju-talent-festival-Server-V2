package team.startup.gwangjutalentfestival.domain.monitoring.client;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import team.startup.gwangjutalentfestival.domain.monitoring.properties.MonitoringProperties;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class DiscordWebhookClient {

    private static final long MIN_SEND_INTERVAL_NANOS = TimeUnit.SECONDS.toNanos(1);

    private final RestClient discordRestClient;
    private final MonitoringProperties monitoringProperties;
    private final Executor discordAlertExecutor;
    private final MeterRegistry meterRegistry;
    private final AtomicLong lastAcceptedNanos = new AtomicLong();

    public DiscordWebhookClient(
            @Qualifier("discordRestClient") RestClient discordRestClient,
            MonitoringProperties monitoringProperties,
            @Qualifier("discordAlertExecutor") Executor discordAlertExecutor,
            MeterRegistry meterRegistry
    ) {
        this.discordRestClient = discordRestClient;
        this.monitoringProperties = monitoringProperties;
        this.discordAlertExecutor = discordAlertExecutor;
        this.meterRegistry = meterRegistry;
    }

    public void send(String message) {
        String webhookUrl = monitoringProperties.discordWebhookUrl();
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return;
        }

        if (!acquireRateLimit()) {
            record("rate_limited");
            return;
        }

        try {
            discordAlertExecutor.execute(() -> send(webhookUrl, message));
        } catch (RejectedExecutionException e) {
            record("rejected");
        }
    }

    private boolean acquireRateLimit() {
        long now = System.nanoTime();
        long previous = lastAcceptedNanos.get();
        while (previous == 0 || now - previous >= MIN_SEND_INTERVAL_NANOS) {
            if (lastAcceptedNanos.compareAndSet(previous, now)) {
                return true;
            }
            previous = lastAcceptedNanos.get();
        }
        return false;
    }

    private void send(String webhookUrl, String message) {
        try {
            discordRestClient.post()
                    .uri(webhookUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("content", message))
                    .retrieve()
                    .toBodilessEntity();
            record("sent");
        } catch (Exception e) {
            record("failed");
            log.warn("Discord 알림 전송 실패.", e);
        }
    }

    private void record(String outcome) {
        meterRegistry.counter("discord.alerts", "outcome", outcome).increment();
    }
}
