package team.startup.gwangjutalentfestival.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Spring {@link RestClient} 빈 설정.
 * <p>연결 타임아웃 5초, 읽기 타임아웃 10초를 적용한다.</p>
 */
@Configuration
public class RestClientConfig {

    private static final int CONNECT_TIMEOUT_MS = 5_000;
    private static final int READ_TIMEOUT_MS = 10_000;

    /**
     * 타임아웃이 설정된 {@link RestClient} 빈을 생성한다.
     *
     * @return {@link RestClient} 빈
     */
    @Bean
    public RestClient restClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(CONNECT_TIMEOUT_MS));
        requestFactory.setReadTimeout(Duration.ofMillis(READ_TIMEOUT_MS));

        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

}
