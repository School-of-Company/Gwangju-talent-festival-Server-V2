package team.startup.gwangjutalentfestival.global.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HttpExceptionHandlerTest {

    private final HttpExceptionHandler handler = new HttpExceptionHandler();

    @Test
    void GlobalException_발생시_해당_ErrorCode의_상태와_메시지를_응답한다() {
        ResponseEntity<ErrorResponse> response =
                handler.globalException(new GlobalException(ErrorCode.TEAM_NOT_FOUND));

        assertThat(response.getStatusCode().value()).isEqualTo(ErrorCode.TEAM_NOT_FOUND.getStatus());
        assertThat(response.getBody().message()).isEqualTo(ErrorCode.TEAM_NOT_FOUND.getMessage());
    }

    @Test
    void MethodArgumentNotValidException_발생시_첫번째_필드_오류_메시지를_응답한다() {
        FieldError fieldError = new FieldError("request", "teamId", "팀 ID를 입력해주세요.");
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        MethodArgumentNotValidException e = mock(MethodArgumentNotValidException.class);
        when(e.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ErrorResponse> response = handler.methodArgumentNotValidException(e);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("팀 ID를 입력해주세요.");
    }

    @Test
    void ConstraintViolationException_발생시_첫번째_위반_메시지를_응답한다() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("좌석 번호는 최소 1번부터 존재합니다.");
        ConstraintViolationException e = new ConstraintViolationException(Set.of(violation));

        ResponseEntity<ErrorResponse> response = handler.constraintViolationException(e);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("좌석 번호는 최소 1번부터 존재합니다.");
    }

    @Test
    void ConstraintViolationException_위반내역이_없으면_기본_메시지를_응답한다() {
        ConstraintViolationException e = new ConstraintViolationException(Set.of());

        ResponseEntity<ErrorResponse> response = handler.constraintViolationException(e);

        assertThat(response.getBody().message()).isEqualTo("잘못된 요청입니다.");
    }

    @Test
    void HttpMessageNotReadableException_발생시_400을_응답한다() {
        ResponseEntity<ErrorResponse> response =
                handler.httpMessageNotReadableException(mock(HttpMessageNotReadableException.class));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("요청 본문을 읽을 수 없습니다.");
    }

    @Test
    void MethodArgumentTypeMismatchException_발생시_400을_응답한다() {
        ResponseEntity<ErrorResponse> response =
                handler.methodArgumentTypeMismatchException(mock(MethodArgumentTypeMismatchException.class));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("요청 파라미터의 타입이 올바르지 않습니다.");
    }

    @Test
    void MissingServletRequestParameterException_발생시_400을_응답한다() {
        MissingServletRequestParameterException e =
                new MissingServletRequestParameterException("teamId", "Long");

        ResponseEntity<ErrorResponse> response = handler.missingServletRequestParameterException(e);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("필수 요청 파라미터가 누락되었습니다.");
    }

    @Test
    void HttpRequestMethodNotSupportedException_발생시_405를_응답한다() {
        HttpRequestMethodNotSupportedException e =
                new HttpRequestMethodNotSupportedException("POST");

        ResponseEntity<ErrorResponse> response = handler.httpRequestMethodNotSupportedException(e);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(response.getBody().message()).isEqualTo("지원하지 않는 HTTP 메서드입니다.");
    }

    @Test
    void NoResourceFoundException_발생시_404를_응답한다() {
        NoResourceFoundException e = new NoResourceFoundException(HttpMethod.GET, "/not-exists");

        ResponseEntity<ErrorResponse> response = handler.noResourceFoundException(e);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().message()).isEqualTo("요청한 리소스를 찾을 수 없습니다.");
    }

    @Test
    void 예상치_못한_예외_발생시_500을_응답한다() {
        ResponseEntity<ErrorResponse> response = handler.exception(new RuntimeException("알 수 없는 오류"));

        assertThat(response.getStatusCode().value()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR.getStatus());
        assertThat(response.getBody().message()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
    }
}
