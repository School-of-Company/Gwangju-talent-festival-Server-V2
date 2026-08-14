package team.startup.gwangjutalentfestival.domain.seat.properties;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class SeatReservationPeriodPropertiesTest {

    @Configuration
    @EnableConfigurationProperties(SeatReservationPeriodProperties.class)
    static class Cfg {
    }

    @Test
    void 설정이_비어있으면_기본_기간이_적용된다() {
        SeatReservationPeriodProperties properties = new SeatReservationPeriodProperties(null, null);

        assertThat(properties.of(Role.PERFORMER).startAt()).isEqualTo(LocalDateTime.of(2026, 8, 14, 14, 0, 0));
        assertThat(properties.of(Role.PERFORMER).endAt()).isEqualTo(LocalDateTime.of(2026, 8, 19, 23, 59, 59));
        assertThat(properties.of(Role.USER).startAt()).isEqualTo(LocalDateTime.of(2026, 8, 20, 19, 0, 0));
        assertThat(properties.of(Role.USER).endAt()).isEqualTo(LocalDateTime.of(2026, 9, 4, 18, 0, 0));
    }

    @Test
    void 기간_설정이_일부만_주어지면_나머지는_기본값으로_채워진다() {
        LocalDateTime customStart = LocalDateTime.of(2026, 1, 1, 0, 0, 0);
        SeatReservationPeriodProperties properties = new SeatReservationPeriodProperties(
                new SeatReservationPeriodProperties.Period(customStart, null), null);

        assertThat(properties.of(Role.PERFORMER).startAt()).isEqualTo(customStart);
        assertThat(properties.of(Role.PERFORMER).endAt()).isEqualTo(LocalDateTime.of(2026, 8, 19, 23, 59, 59));
    }

    @Test
    void 빈_문자열_env여도_기동되고_기본값으로_폴백된다() {
        new ApplicationContextRunner()
                .withUserConfiguration(Cfg.class)
                .withPropertyValues(
                        "seat.reservation.performer.start-at=",
                        "seat.reservation.performer.end-at=",
                        "seat.reservation.user.start-at=",
                        "seat.reservation.user.end-at="
                )
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    SeatReservationPeriodProperties props = context.getBean(SeatReservationPeriodProperties.class);
                    assertThat(props.of(Role.PERFORMER).startAt())
                            .isEqualTo(LocalDateTime.of(2026, 8, 14, 14, 0, 0));
                    assertThat(props.of(Role.USER).endAt())
                            .isEqualTo(LocalDateTime.of(2026, 9, 4, 18, 0, 0));
                });
    }

    @Test
    void 설정_자체가_없어도_기동된다() {
        new ApplicationContextRunner()
                .withUserConfiguration(Cfg.class)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.getBean(SeatReservationPeriodProperties.class).of(Role.USER).startAt())
                            .isEqualTo(LocalDateTime.of(2026, 8, 20, 19, 0, 0));
                });
    }
}
