package team.startup.gwangjutalentfestival.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 애플리케이션 전역 비즈니스 예외 기반 클래스.
 * <p>모든 도메인별 예외는 이 클래스를 상속하며, {@link ErrorCode}를 통해 에러 정보를 전달한다.</p>
 */
@Getter
@RequiredArgsConstructor
public class GlobalException extends RuntimeException {
    private final ErrorCode errorCode;
}

