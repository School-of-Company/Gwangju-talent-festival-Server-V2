package team.startup.gwangjutalentfestival.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import team.startup.gwangjutalentfestival.domain.monitoring.properties.MonitoringProperties;

@Configuration
public class MonitoringClientConfig {

    @Bean
    public RestClient prometheusRestClient(MonitoringProperties monitoringProperties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(5000);
        return RestClient.builder()
                .baseUrl(monitoringProperties.prometheusBaseUrl())
                .requestFactory(factory)
                .build();
    }

    @Bean
    public RestClient discordRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(5000);
        return RestClient.builder()
                .requestFactory(factory)
                .build();
    }
}
