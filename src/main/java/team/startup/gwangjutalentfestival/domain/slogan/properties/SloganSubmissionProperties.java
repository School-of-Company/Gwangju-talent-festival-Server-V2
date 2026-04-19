package team.startup.gwangjutalentfestival.domain.slogan.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.LocalDateTime;

/**
 * 슬로건 접수 기간 설정 프로퍼티.
 * {@code slogan.submission} 접두사로 외부 설정에서 주입된다.
 *
 * @param startAt 접수 시작 일시
 * @param endAt   접수 종료 일시
 */
@ConfigurationProperties(prefix = "slogan.submission")
public record SloganSubmissionProperties(
        LocalDateTime startAt,
        LocalDateTime endAt
) {
}
