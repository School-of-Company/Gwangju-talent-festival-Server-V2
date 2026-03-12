package team.startup.gwangjutalentfestival;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import team.startup.gwangjutalentfestival.global.jwt.JwtProperties;
import team.startup.gwangjutalentfestival.global.security.properties.CorsProperties;
import team.startup.gwangjutalentfestival.global.sms.properties.SmsVerifyProperties;
import team.startup.gwangjutalentfestival.global.sms.properties.SolapiProperties;

@EnableFeignClients
@EnableConfigurationProperties({JwtProperties.class, CorsProperties.class, SolapiProperties.class, SmsVerifyProperties.class})
@SpringBootApplication
public class GwangjutalentfestivalApplication {

	public static void main(String[] args) {
		SpringApplication.run(GwangjutalentfestivalApplication.class, args);
	}

}
