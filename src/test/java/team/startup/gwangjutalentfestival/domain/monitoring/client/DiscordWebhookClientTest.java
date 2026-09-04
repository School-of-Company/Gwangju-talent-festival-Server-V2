package team.startup.gwangjutalentfestival.domain.monitoring.client;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import team.startup.gwangjutalentfestival.domain.monitoring.properties.MonitoringProperties;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class DiscordWebhookClientTest {

    @Test
    void 연속_알림은_초당_한_건만_executor에_전달한다() {
        Executor executor = mock(Executor.class);
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        DiscordWebhookClient client = client(executor, meterRegistry);

        client.send("first");
        client.send("second");

        verify(executor).execute(org.mockito.ArgumentMatchers.any(Runnable.class));
        verifyNoMoreInteractions(executor);
        assertThat(meterRegistry.counter("discord.alerts", "outcome", "rate_limited").count()).isEqualTo(1);
    }

    @Test
    void executor가_포화되면_요청을_막지_않고_폐기한다() {
        Executor executor = mock(Executor.class);
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        doThrow(new RejectedExecutionException()).when(executor)
                .execute(org.mockito.ArgumentMatchers.any(Runnable.class));
        DiscordWebhookClient client = client(executor, meterRegistry);

        client.send("alert");

        assertThat(meterRegistry.counter("discord.alerts", "outcome", "rejected").count()).isEqualTo(1);
    }

    private DiscordWebhookClient client(Executor executor, SimpleMeterRegistry meterRegistry) {
        return new DiscordWebhookClient(
                mock(RestClient.class),
                new MonitoringProperties(true, "http://localhost", "http://localhost/webhook"),
                executor,
                meterRegistry
        );
    }
}
