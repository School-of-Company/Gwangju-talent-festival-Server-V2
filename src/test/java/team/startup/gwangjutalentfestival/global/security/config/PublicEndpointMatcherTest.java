package team.startup.gwangjutalentfestival.global.security.config;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class PublicEndpointMatcherTest {

    @ParameterizedTest(name = "{0} {1} 은 공개 엔드포인트이다")
    @CsvSource({
            "POST, /auth/verify",
            "POST, /auth/join",
            "POST, /auth/login",
            "PATCH, /auth/refresh",
            "GET, /actuator/health",
            "GET, /actuator/prometheus",
            "GET, /vote/team-1",
            "GET, /error",
            "GET, /swagger-ui/index.html",
            "GET, /v3/api-docs",
            "GET, /team",
            "POST, /apply",
            "POST, /apply/upload/initiate",
            "GET, /apply/123/video",
            "POST, /slogan"
    })
    void 명시된_경로는_공개_엔드포인트로_분류된다(String method, String path) {
        MockHttpServletRequest request = request(method, path);

        assertThat(PublicEndpointMatcher.matcher().matches(request)).isTrue();
    }

    @ParameterizedTest(name = "{0} {1} 은 공개 엔드포인트가 아니다")
    @CsvSource({
            "DELETE, /auth/logout",
            "GET, /excel/summary",
            "GET, /excel/judge-sheets",
            "GET, /judge",
            "GET, /seat",
            "GET, /monitoring/anomalies",
            "DELETE, /team/1"
    })
    void 명시되지_않은_경로는_공개_엔드포인트가_아니다(String method, String path) {
        MockHttpServletRequest request = request(method, path);

        assertThat(PublicEndpointMatcher.matcher().matches(request)).isFalse();
    }

    private MockHttpServletRequest request(String method, String path) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setServletPath(path);
        return request;
    }
}
