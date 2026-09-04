package team.startup.gwangjutalentfestival.domain.monitoring.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import team.startup.gwangjutalentfestival.domain.monitoring.client.DiscordWebhookClient;
import team.startup.gwangjutalentfestival.domain.monitoring.properties.RequestAlertProperties;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 요청당 한 번 실행되며, 에러 응답이거나 지정된 임계값을 넘는 지연 요청을 즉시 디스코드로 알린다.
 * <p>Spring Security 필터 체인보다 앞서 동작하도록 최우선 순위로 등록되어, 인증 실패(401/403)를 포함한
 * 전체 요청 처리 시간을 측정한다.</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestAlertFilter extends OncePerRequestFilter {

    private static final ZoneId ZONE_SEOUL = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RequestAlertProperties requestAlertProperties;
    private final DiscordWebhookClient discordWebhookClient;
    private final String appEnvironment;

    public RequestAlertFilter(
            RequestAlertProperties requestAlertProperties,
            DiscordWebhookClient discordWebhookClient,
            @Value("${APP_ENV:LOCAL}") String appEnvironment
    ) {
        this.requestAlertProperties = requestAlertProperties;
        this.discordWebhookClient = discordWebhookClient;
        this.appEnvironment = appEnvironment;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        if (!requestAlertProperties.enabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMillis = System.currentTimeMillis() - start;
            int status = response.getStatus();
            boolean isError = status >= 500;
            boolean isSlow = durationMillis >= requestAlertProperties.slowThresholdMillis();

            if (isError || isSlow) {
                discordWebhookClient.send(buildMessage(request, status, durationMillis, isError));
            }
        }
    }

    private String buildMessage(HttpServletRequest request, int status, long durationMillis, boolean isError) {
        String emoji = isError ? "🔥" : "🐢";
        String tag = isError ? "ERROR" : "SLOW";
        String time = LocalDateTime.now(ZONE_SEOUL).format(TIME_FORMATTER);
        return "%s [%s][%s] %s | %s %s → %d (%dms)".formatted(
                emoji, appEnvironment, tag, time, request.getMethod(), request.getRequestURI(), status, durationMillis
        );
    }
}
