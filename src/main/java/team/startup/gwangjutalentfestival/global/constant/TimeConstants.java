package team.startup.gwangjutalentfestival.global.constant;

import java.time.ZoneId;

/**
 * 시간 관련 전역 상수 클래스.
 * <p>서울(KST) 타임존 등 애플리케이션 전반에서 공통으로 사용하는 시간 상수를 정의한다.</p>
 */
public final class TimeConstants {

    /** 서울(Asia/Seoul, KST) 타임존 ID */
    public static final ZoneId SEOUL_ZONE_ID = ZoneId.of("Asia/Seoul");

    private TimeConstants() {
    }
}
