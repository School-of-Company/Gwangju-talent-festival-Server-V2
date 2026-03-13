package team.startup.gwangjutalentfestival;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import team.startup.gwangjutalentfestival.domain.slogan.properties.SloganSubmissionProperties;
import team.startup.gwangjutalentfestival.global.jwt.JwtProperties;
import team.startup.gwangjutalentfestival.global.security.properties.CorsProperties;

@EnableFeignClients
@EnableConfigurationProperties({JwtProperties.class, CorsProperties.class, SloganSubmissionProperties.class})
@SpringBootApplication
public class GwangjutalentfestivalApplication {

    public static void main(String[] args) {
        SpringApplication.run(GwangjutalentfestivalApplication.class, args);
    }

}
