package team.startup.gwangjutalentfestival;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import team.startup.gwangjutalentfestival.domain.monitoring.properties.DatasetProperties;
import team.startup.gwangjutalentfestival.domain.monitoring.properties.MlProperties;
import team.startup.gwangjutalentfestival.domain.monitoring.properties.MonitoringProperties;
import team.startup.gwangjutalentfestival.domain.monitoring.properties.RequestAlertProperties;
import team.startup.gwangjutalentfestival.domain.judge.properties.JudgeStrokesProperties;
import team.startup.gwangjutalentfestival.domain.seat.properties.SeatReservationPeriodProperties;
import team.startup.gwangjutalentfestival.domain.slogan.properties.SloganSubmissionProperties;
import team.startup.gwangjutalentfestival.global.jwt.JwtProperties;
import team.startup.gwangjutalentfestival.global.s3.properties.AwsS3Properties;
import team.startup.gwangjutalentfestival.global.security.properties.CorsProperties;
import team.startup.gwangjutalentfestival.global.sms.properties.SmsVerifyProperties;
import team.startup.gwangjutalentfestival.global.sms.properties.SolapiProperties;

/**
 * 광주 탤런트 페스티벌 서버 애플리케이션의 진입점.
 * <p>비동기 처리, 스케줄링, OpenFeign 클라이언트, 설정 프로퍼티를 활성화한다.</p>
 */
@EnableFeignClients
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties({JwtProperties.class, CorsProperties.class, SolapiProperties.class, SmsVerifyProperties.class, SloganSubmissionProperties.class, SeatReservationPeriodProperties.class, MonitoringProperties.class, DatasetProperties.class, MlProperties.class, AwsS3Properties.class, RequestAlertProperties.class, JudgeStrokesProperties.class})
@SpringBootApplication
public class GwangjutalentfestivalApplication {

    /**
     * 애플리케이션을 시작한다.
     *
     * @param args 커맨드라인 인수
     */
    public static void main(String[] args) {
        SpringApplication.run(GwangjutalentfestivalApplication.class, args);
    }

}
