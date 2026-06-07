package team.startup.gwangjutalentfestival.domain.monitoring.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import team.startup.gwangjutalentfestival.domain.monitoring.client.dto.MlAnomalyScoreRequest;
import team.startup.gwangjutalentfestival.domain.monitoring.client.dto.MlAnomalyScoreResponse;
import team.startup.gwangjutalentfestival.domain.monitoring.properties.MlProperties;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MlServerClientTest {

    @Mock
    private RestClient mlRestClient;

    private MlAnomalyScoreRequest request;

    @BeforeEach
    void setUp() {
        request = new MlAnomalyScoreRequest("SEAT", "failure_rate", 0.08, 10, 3);
    }

    @Test
    void enabled가_false이면_HTTP_호출_없이_Optional_empty_반환() {
        MlServerClient client = new MlServerClient(mlRestClient, new MlProperties(false, "http://localhost:8000", 3000));

        Optional<MlAnomalyScoreResponse> result = client.call(request);

        assertThat(result).isEmpty();
        verify(mlRestClient, never()).post();
    }

    @Test
    void baseUrl이_blank이면_HTTP_호출_없이_Optional_empty_반환() {
        MlServerClient client = new MlServerClient(mlRestClient, new MlProperties(true, "", 3000));

        Optional<MlAnomalyScoreResponse> result = client.call(request);

        assertThat(result).isEmpty();
        verify(mlRestClient, never()).post();
    }

    @Test
    void modelLoaded가_false이면_Optional_empty_반환() {
        MlAnomalyScoreResponse response = new MlAnomalyScoreResponse(0.08, "anomaly", "v1", false);
        RestClient deepMock = stubChain("http://localhost:8000", response);
        MlServerClient client = new MlServerClient(deepMock, new MlProperties(true, "http://localhost:8000", 3000));

        assertThat(client.call(request)).isEmpty();
    }

    @Test
    void anomalyScore가_null이면_Optional_empty_반환() {
        MlAnomalyScoreResponse response = new MlAnomalyScoreResponse(null, "anomaly", "v1", true);
        RestClient deepMock = stubChain("http://localhost:8000", response);
        MlServerClient client = new MlServerClient(deepMock, new MlProperties(true, "http://localhost:8000", 3000));

        assertThat(client.call(request)).isEmpty();
    }

    @Test
    void predictedLabel이_유효하지_않으면_Optional_empty_반환() {
        MlAnomalyScoreResponse response = new MlAnomalyScoreResponse(0.08, "invalid", "v1", true);
        RestClient deepMock = stubChain("http://localhost:8000", response);
        MlServerClient client = new MlServerClient(deepMock, new MlProperties(true, "http://localhost:8000", 3000));

        assertThat(client.call(request)).isEmpty();
    }

    @Test
    void 정상_응답이면_Optional_of_response_반환() {
        MlAnomalyScoreResponse response = new MlAnomalyScoreResponse(0.0794, "anomaly", "iforest-v1", true);
        RestClient deepMock = stubChain("http://localhost:8000", response);
        MlServerClient client = new MlServerClient(deepMock, new MlProperties(true, "http://localhost:8000", 3000));

        Optional<MlAnomalyScoreResponse> result = client.call(request);

        assertThat(result).isPresent();
        assertThat(result.get().anomalyScore()).isEqualTo(0.0794);
        assertThat(result.get().predictedLabel()).isEqualTo("anomaly");
        assertThat(result.get().modelVersion()).isEqualTo("iforest-v1");
    }

    @Test
    void HTTP_예외_발생시_Optional_empty_반환() {
        RestClient deepMock = mock(RestClient.class, RETURNS_DEEP_STUBS);
        given(deepMock.post()
                .uri("http://localhost:8000/anomaly-score")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(MlAnomalyScoreResponse.class))
                .willThrow(new RuntimeException("connection refused"));
        MlServerClient client = new MlServerClient(deepMock, new MlProperties(true, "http://localhost:8000", 3000));

        assertThat(client.call(request)).isEmpty();
    }

    @Test
    void baseUrl에_trailing_slash가_있어도_double_slash_없이_정상_호출됨() {
        MlAnomalyScoreResponse response = new MlAnomalyScoreResponse(0.05, "normal", "v1", true);
        RestClient deepMock = stubChain("http://localhost:8000", response);
        MlServerClient client = new MlServerClient(deepMock, new MlProperties(true, "http://localhost:8000/", 3000));

        Optional<MlAnomalyScoreResponse> result = client.call(request);

        assertThat(result).isPresent();
    }

    @Test
    void timeoutMs가_0이하이면_3000으로_보정됨() {
        MlProperties props = new MlProperties(true, "http://localhost:8000", 0);
        assertThat(props.timeoutMs()).isEqualTo(3000L);
    }

    @Test
    void timeoutMs가_음수이면_3000으로_보정됨() {
        MlProperties props = new MlProperties(true, "http://localhost:8000", -1);
        assertThat(props.timeoutMs()).isEqualTo(3000L);
    }

    private RestClient stubChain(String baseUrl, MlAnomalyScoreResponse response) {
        RestClient deepMock = mock(RestClient.class, RETURNS_DEEP_STUBS);
        given(deepMock.post()
                .uri(baseUrl + "/anomaly-score")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(MlAnomalyScoreResponse.class))
                .willReturn(response);
        return deepMock;
    }
}
