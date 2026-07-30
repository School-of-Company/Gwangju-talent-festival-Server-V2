package team.startup.gwangjutalentfestival.domain.monitoring.filter;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import team.startup.gwangjutalentfestival.domain.monitoring.client.DiscordWebhookClient;
import team.startup.gwangjutalentfestival.domain.monitoring.properties.RequestAlertProperties;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RequestAlertFilterTest {

    @Mock
    private DiscordWebhookClient discordWebhookClient;

    @Mock
    private FilterChain filterChain;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest("GET", "/team");
        response = new MockHttpServletResponse();
    }

    @Test
    void 알림이_비활성화되어_있으면_에러가_발생해도_전송하지_않는다() throws Exception {
        RequestAlertFilter filter = new RequestAlertFilter(
                new RequestAlertProperties(false, 3_000), discordWebhookClient
        );
        doAnswer(invocation -> {
            response.setStatus(500);
            return null;
        }).when(filterChain).doFilter(request, response);

        filter.doFilter(request, response, filterChain);

        verify(discordWebhookClient, never()).send(anyString());
    }

    @Test
    void 정상_응답이고_임계값보다_빠르면_전송하지_않는다() throws Exception {
        RequestAlertFilter filter = new RequestAlertFilter(
                new RequestAlertProperties(true, 3_000), discordWebhookClient
        );
        doAnswer(invocation -> {
            response.setStatus(200);
            return null;
        }).when(filterChain).doFilter(request, response);

        filter.doFilter(request, response, filterChain);

        verify(discordWebhookClient, never()).send(anyString());
    }

    @Test
    void 에러_응답이면_디스코드로_전송한다() throws Exception {
        RequestAlertFilter filter = new RequestAlertFilter(
                new RequestAlertProperties(true, 999_999), discordWebhookClient
        );
        doAnswer(invocation -> {
            response.setStatus(500);
            return null;
        }).when(filterChain).doFilter(request, response);

        filter.doFilter(request, response, filterChain);

        verify(discordWebhookClient).send(anyString());
    }

    @Test
    void 임계값을_초과하는_느린_요청이면_디스코드로_전송한다() throws Exception {
        RequestAlertFilter filter = new RequestAlertFilter(
                new RequestAlertProperties(true, 0), discordWebhookClient
        );
        doAnswer(invocation -> {
            response.setStatus(200);
            return null;
        }).when(filterChain).doFilter(request, response);

        filter.doFilter(request, response, filterChain);

        verify(discordWebhookClient).send(anyString());
    }
}
