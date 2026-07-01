package team.startup.gwangjutalentfestival.global.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import team.startup.gwangjutalentfestival.global.exception.ErrorCode;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAccessDeniedHandlerTest {

    private final JwtAccessDeniedHandler accessDeniedHandler =
            new JwtAccessDeniedHandler(new ObjectMapper());

    @Test
    void 권한이_없는_요청은_403과_FORBIDDEN_에러코드를_응답한다() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        accessDeniedHandler.handle(request, response, new AccessDeniedException("접근 거부"));

        assertThat(response.getStatus()).isEqualTo(ErrorCode.FORBIDDEN.getStatus());
        assertThat(response.getContentType()).startsWith("application/json");
        assertThat(response.getContentAsString()).contains(ErrorCode.FORBIDDEN.getMessage());
    }
}
