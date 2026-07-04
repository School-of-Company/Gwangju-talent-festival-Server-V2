package team.startup.gwangjutalentfestival.global.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.method.HandlerMethod;
import team.startup.gwangjutalentfestival.global.util.UserUtil;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
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

    static class TestController {
        @RateLimited(key = "seat:reservation")
        public void rateLimited() {}

        public void plain() {}
    }

    @Test
    void 쿨다운_키_선점에_성공하면_요청이_허용된다() {
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).willReturn(true);

        try (MockedStatic<UserUtil> userUtil = mockStatic(UserUtil.class)) {
            userUtil.when(UserUtil::getCurrentUserId).thenReturn(USER_ID);

            assertThat(interceptor.preHandle(request, response, rateLimitedHandler)).isTrue();
        }
    }

    @Test
    void 쿨다운_내_재요청이면_TooManyRequestsException이_발생한다() {
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).willReturn(false);

        try (MockedStatic<UserUtil> userUtil = mockStatic(UserUtil.class)) {
            userUtil.when(UserUtil::getCurrentUserId).thenReturn(USER_ID);

            assertThatThrownBy(() -> interceptor.preHandle(request, response, rateLimitedHandler))
                    .isInstanceOf(TooManyRequestsException.class);
        }
    }

    @Test
    void Redis_장애_시_요청이_허용된다() {
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .willThrow(new QueryTimeoutException("Redis timeout"));

        try (MockedStatic<UserUtil> userUtil = mockStatic(UserUtil.class)) {
            userUtil.when(UserUtil::getCurrentUserId).thenReturn(USER_ID);

            assertThat(interceptor.preHandle(request, response, rateLimitedHandler)).isTrue();
        }
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
