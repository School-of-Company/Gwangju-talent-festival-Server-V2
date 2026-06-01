package team.startup.gwangjutalentfestival.domain.monitoring.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class PrometheusClientRangeQueryTest {

    @Mock
    private RestClient prometheusRestClient;

    private PrometheusClient prometheusClient;

    @BeforeEach
    void setUp() {
        prometheusClient = new PrometheusClient(prometheusRestClient, "gwangjutalentfestival");
    }

    @Test
    void 정상_range_query_응답이면_PrometheusRangePoint_목록을_반환한다() {
        PrometheusRangeSeries series = new PrometheusRangeSeries(
                null,
                List.of(
                        List.of(1748736000L, "0.023"),
                        List.of(1748736060L, "0.045")
                )
        );
        PrometheusRangeResponse response = new PrometheusRangeResponse(
                "success",
                new PrometheusRangeData(List.of(series))
        );

        RestClient.RequestHeadersUriSpec uriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec headersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        given(prometheusRestClient.get()).willReturn(uriSpec);
        given(uriSpec.uri(any(java.util.function.Function.class))).willReturn(headersSpec);
        given(headersSpec.retrieve()).willReturn(responseSpec);
        given(responseSpec.body(PrometheusRangeResponse.class)).willReturn(response);

        List<PrometheusRangePoint> result = prometheusClient.queryRange(
                "rate(seat_reservation_failure_total[5m])", 1748736000L, 1748736120L, 60
        );

        assertThat(result).hasSize(2);
        assertThat(result.get(0).timestamp()).isEqualTo(1748736000L);
        assertThat(result.get(0).value()).isEqualTo(0.023);
        assertThat(result.get(1).timestamp()).isEqualTo(1748736060L);
        assertThat(result.get(1).value()).isEqualTo(0.045);
    }

    @Test
    void NaN_값이_포함된_경우_해당_포인트를_skip한다() {
        PrometheusRangeSeries series = new PrometheusRangeSeries(
                null,
                List.of(
                        List.of(1748736000L, "NaN"),
                        List.of(1748736060L, "0.012")
                )
        );
        PrometheusRangeResponse response = new PrometheusRangeResponse(
                "success",
                new PrometheusRangeData(List.of(series))
        );

        RestClient.RequestHeadersUriSpec uriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec headersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        given(prometheusRestClient.get()).willReturn(uriSpec);
        given(uriSpec.uri(any(java.util.function.Function.class))).willReturn(headersSpec);
        given(headersSpec.retrieve()).willReturn(responseSpec);
        given(responseSpec.body(PrometheusRangeResponse.class)).willReturn(response);

        List<PrometheusRangePoint> result = prometheusClient.queryRange(
                "rate(seat_reservation_failure_total[5m])", 1748736000L, 1748736120L, 60
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).timestamp()).isEqualTo(1748736060L);
    }

    @Test
    void Prometheus_호출_실패_시_빈_리스트를_반환한다() {
        RestClient.RequestHeadersUriSpec uriSpec = mock(RestClient.RequestHeadersUriSpec.class);

        given(prometheusRestClient.get()).willReturn(uriSpec);
        given(uriSpec.uri(any(java.util.function.Function.class))).willThrow(new RuntimeException("connection refused"));

        List<PrometheusRangePoint> result = prometheusClient.queryRange(
                "rate(seat_reservation_failure_total[5m])", 1748736000L, 1748736120L, 60
        );

        assertThat(result).isEmpty();
    }
}