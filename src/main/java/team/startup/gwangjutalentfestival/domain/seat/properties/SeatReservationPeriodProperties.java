package team.startup.gwangjutalentfestival.domain.seat.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;

import java.time.LocalDateTime;

/**
 * 좌석 예매 가능 기간 설정 프로퍼티.
 * <p>{@code seat.reservation} 접두사로 외부 설정에서 주입되며, 참가자와 일반 유저의 기간을 분리해 관리한다.
 * 설정이 누락되거나 일부만 지정된 경우 확정 일정 상수로 대체하므로 기동에 실패하지 않는다.</p>
 *
 * @param performer 참가자(PERFORMER) 예매 기간
 * @param user      일반 유저 예매 기간
 */
@ConfigurationProperties(prefix = "seat.reservation")
public record SeatReservationPeriodProperties(
        Period performer,
        Period user
) {

    private static final Period DEFAULT_PERFORMER = new Period(
            LocalDateTime.of(2026, 8, 14, 14, 0, 0),
            LocalDateTime.of(2026, 8, 19, 23, 59, 59));

    private static final Period DEFAULT_USER = new Period(
            LocalDateTime.of(2026, 8, 20, 19, 0, 0),
            LocalDateTime.of(2026, 9, 4, 18, 0, 0));

    public SeatReservationPeriodProperties {
        // ponytail: 설정 누락 시 기동 실패 대신 확정 일정으로 폴백한다. 운영 중 값이 비어도 예매가 멈추지 않는다.
        performer = merge(performer, DEFAULT_PERFORMER);
        user = merge(user, DEFAULT_USER);
    }

    private static Period merge(Period value, Period fallback) {
        if (value == null) return fallback;
        return new Period(
                value.startAt() != null ? value.startAt() : fallback.startAt(),
                value.endAt() != null ? value.endAt() : fallback.endAt());
    }

    /**
     * 역할에 해당하는 예매 기간을 반환한다.
     *
     * @param role 사용자 역할
     * @return 참가자면 참가자 기간, 그 외에는 일반 유저 기간
     */
    public Period of(Role role) {
        return role == Role.PERFORMER ? performer : user;
    }

    /**
     * 예매 가능 기간 구간.
     *
     * @param startAt 시작 일시
     * @param endAt   종료 일시
     */
    public record Period(LocalDateTime startAt, LocalDateTime endAt) {

        public boolean contains(LocalDateTime now) {
            return !now.isBefore(startAt) && !now.isAfter(endAt);
        }
    }
}
