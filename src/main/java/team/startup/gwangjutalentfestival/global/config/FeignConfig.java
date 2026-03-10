package team.startup.gwangjutalentfestival.global.config;

import feign.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Value("${feign.log-level:BASIC}")
    private String logLevel;

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.valueOf(logLevel);
    }
}
