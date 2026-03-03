package team.startup.gwangjutalentfestival;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import team.startup.gwangjutalentfestival.global.jwt.JwtProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class GwangjutalentfestivalApplication {

	public static void main(String[] args) {
		SpringApplication.run(GwangjutalentfestivalApplication.class, args);
	}

}
