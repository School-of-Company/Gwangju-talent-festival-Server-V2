package team.startup.gwangjutalentfestival.global.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.method.HandlerMethod;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;
import team.startup.gwangjutalentfestival.global.auth.CustomUserDetails;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class RateLimitInterceptorTest {

    @InjectMocks
    private RateLimitInterceptor interceptor;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private final HttpServletResponse response = mock(HttpServletResponse.class);

    private static final long USER_ID = 1L;

    private HandlerMethod rateLimitedHandler;
    private HandlerMethod plainHandler;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        ReflectionTestUtils.setField(interceptor, "cooldownSeconds", 1L);
        rateLimitedHandler = new HandlerMethod(new TestController(), TestController.class.getMethod("rateLimited"));
        plainHandler = new HandlerMethod(new TestController(), TestController.class.getMethod("plain"));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticate() {
        CustomUserDetails userDetails = CustomUserDetails.fromToken(USER_ID, Role.USER);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
    }

    static class TestController {
        @RateLimited(key = "seat:reservation")
        public void rateLimited() {}

        public void plain() {}
    }

    @Test
    void 쿨다운_키_선점에_성공하면_요청이_허용된다() {
        authenticate();
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).willReturn(true);

        assertThat(interceptor.preHandle(request, response, rateLimitedHandler)).isTrue();

        verify(valueOperations).setIfAbsent("rate-limit:seat:reservation:" + USER_ID, "1", Duration.ofSeconds(1));
    }

    @Test
    void 쿨다운_내_재요청이면_TooManyRequestsException이_발생한다() {
        authenticate();
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).willReturn(false);

        assertThatThrownBy(() -> interceptor.preHandle(request, response, rateLimitedHandler))
                .isInstanceOf(TooManyRequestsException.class);
    }

    @Test
    void Redis_장애_시_요청이_허용된다() {
        authenticate();
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .willThrow(new QueryTimeoutException("Redis timeout"));

        assertThat(interceptor.preHandle(request, response, rateLimitedHandler)).isTrue();
    }

    @Test
    void 인증_정보가_없으면_클라이언트_IP를_식별자로_사용한다() {
        given(request.getHeader("X-Forwarded-For")).willReturn(null);
        given(request.getRemoteAddr()).willReturn("10.0.0.7");
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).willReturn(true);

        assertThat(interceptor.preHandle(request, response, rateLimitedHandler)).isTrue();

        verify(valueOperations).setIfAbsent("rate-limit:seat:reservation:10.0.0.7", "1", Duration.ofSeconds(1));
    }

    @Test
    void XForwardedFor_헤더가_있으면_첫_번째_IP를_식별자로_사용한다() {
        given(request.getHeader("X-Forwarded-For")).willReturn("203.0.113.5, 70.41.3.18");
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).willReturn(true);

        assertThat(interceptor.preHandle(request, response, rateLimitedHandler)).isTrue();

        verify(valueOperations).setIfAbsent("rate-limit:seat:reservation:203.0.113.5", "1", Duration.ofSeconds(1));
    }

    @Test
    void 쿨다운_설정이_0_이하면_1초로_보정된다() {
        authenticate();
        ReflectionTestUtils.setField(interceptor, "cooldownSeconds", 0L);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).willReturn(true);

        assertThat(interceptor.preHandle(request, response, rateLimitedHandler)).isTrue();

        ArgumentCaptor<Duration> durationCaptor = ArgumentCaptor.forClass(Duration.class);
        verify(valueOperations).setIfAbsent(anyString(), anyString(), durationCaptor.capture());
        assertThat(durationCaptor.getValue()).isEqualTo(Duration.ofSeconds(1));
    }

    @Test
    void RateLimited가_없는_핸들러는_Redis를_조회하지_않고_허용된다() {
        assertThat(interceptor.preHandle(request, response, plainHandler)).isTrue();
        verifyNoInteractions(redisTemplate);
    }

    @Test
    void HandlerMethod가_아닌_핸들러는_허용된다() {
        assertThat(interceptor.preHandle(request, response, new Object())).isTrue();
        verifyNoInteractions(redisTemplate);
    }
}
