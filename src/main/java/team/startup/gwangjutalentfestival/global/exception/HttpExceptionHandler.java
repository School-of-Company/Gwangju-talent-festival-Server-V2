package team.startup.gwangjutalentfestival.global.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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
     * {@link ConstraintViolationException} 발생 시 첫 번째 위반 메시지를 반환한다.
     *
     * @param e 발생한 {@link ConstraintViolationException}
     * @return 400 Bad Request {@link ErrorResponse}
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> constraintViolationException(ConstraintViolationException e) {
        String reason = e.getConstraintViolations()
                .stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse("잘못된 요청입니다.");
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), reason));
    }

    /**
     * 요청 본문을 읽을 수 없을 때({@link HttpMessageNotReadableException}) 400 Bad Request를 반환한다.
     *
     * @param e 발생한 {@link HttpMessageNotReadableException}
     * @return 400 {@link ErrorResponse}
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> httpMessageNotReadableException(HttpMessageNotReadableException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "요청 본문을 읽을 수 없습니다."));
    }

    /**
     * 요청 파라미터의 타입이 올바르지 않을 때({@link MethodArgumentTypeMismatchException}) 400 Bad Request를 반환한다.
     *
     * @param e 발생한 {@link MethodArgumentTypeMismatchException}
     * @return 400 {@link ErrorResponse}
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> methodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "요청 파라미터의 타입이 올바르지 않습니다."));
    }

    /**
     * 필수 요청 파라미터가 누락되었을 때({@link MissingServletRequestParameterException}) 400 Bad Request를 반환한다.
     *
     * @param e 발생한 {@link MissingServletRequestParameterException}
     * @return 400 {@link ErrorResponse}
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> missingServletRequestParameterException(MissingServletRequestParameterException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "필수 요청 파라미터가 누락되었습니다."));
    }

    /**
     * 지원하지 않는 HTTP 메서드로 요청했을 때({@link HttpRequestMethodNotSupportedException}) 405 Method Not Allowed를 반환한다.
     *
     * @param e 발생한 {@link HttpRequestMethodNotSupportedException}
     * @return 405 {@link ErrorResponse}
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> httpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(new ErrorResponse(HttpStatus.METHOD_NOT_ALLOWED.value(), "지원하지 않는 HTTP 메서드입니다."));
    }

    /**
     * 존재하지 않는 리소스를 요청했을 때({@link NoResourceFoundException}) 404 Not Found를 반환한다.
     *
     * @param e 발생한 {@link NoResourceFoundException}
     * @return 404 {@link ErrorResponse}
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> noResourceFoundException(NoResourceFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "요청한 리소스를 찾을 수 없습니다."));
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
