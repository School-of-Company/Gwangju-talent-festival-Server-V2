package team.startup.gwangjutalentfestival;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import team.startup.gwangjutalentfestival.global.jwt.JwtProperties;
import team.startup.gwangjutalentfestival.global.security.properties.CorsProperties;

@EnableConfigurationProperties({JwtProperties.class,CorsProperties.class})
@SpringBootApplication(exclude = {
        OAuth2ClientAutoConfiguration.class
})
public class GwangjutalentfestivalApplication {

	public static void main(String[] args) {
		SpringApplication.run(GwangjutalentfestivalApplication.class, args);
	}

}
