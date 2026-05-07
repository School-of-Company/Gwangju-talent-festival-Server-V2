package team.startup.gwangjutalentfestival.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 핸들러.
 * <p>{@link GlobalException}, {@link MethodArgumentNotValidException}, 그 외 미처리 예외를 처리하여
 * 일관된 {@link ErrorResponse} 형태로 응답한다.</p>
 */
@Slf4j
@RestControllerAdvice
public class HttpExceptionHandler {

    /**
     * {@link GlobalException} 발생 시 해당 에러 코드에 맞는 응답을 반환한다.
     *
     * @param e 발생한 {@link GlobalException}
     * @return 에러 코드에 맞는 {@link ErrorResponse}
     */
    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<ErrorResponse> globalException(GlobalException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new ErrorResponse(errorCode.getStatus(), errorCode.getMessage()));
    }

    /**
     * {@link MethodArgumentNotValidException} 발생 시 첫 번째 필드 오류 메시지를 반환한다.
     *
     * @param e 발생한 {@link MethodArgumentNotValidException}
     * @return 400 Bad Request {@link ErrorResponse}
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> methodArgumentNotValidException(MethodArgumentNotValidException e) {
        String reason = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("잘못된 요청입니다.");
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), reason));
    }

    /**
     * 예상치 못한 모든 예외를 처리하여 500 Internal Server Error를 반환한다.
     *
     * @param e 발생한 예외
     * @return 500 {@link ErrorResponse}
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> exception(Exception e) {
        log.error("[예상치 못한 예외 발생] message: {}", e.getMessage(), e);
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new ErrorResponse(errorCode.getStatus(), errorCode.getMessage()));
    }
}
