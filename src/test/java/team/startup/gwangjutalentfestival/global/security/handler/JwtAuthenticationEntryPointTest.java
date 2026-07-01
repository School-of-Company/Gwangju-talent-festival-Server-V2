package team.startup.gwangjutalentfestival.global.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import team.startup.gwangjutalentfestival.global.exception.ErrorCode;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthenticationEntryPointTest {

    private final JwtAuthenticationEntryPoint entryPoint =
            new JwtAuthenticationEntryPoint(new ObjectMapper());

    @Test
    void 인증되지_않은_요청은_401과_UNAUTHORIZED_에러코드를_응답한다() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new BadCredentialsException("인증 실패"));

        assertThat(response.getStatus()).isEqualTo(ErrorCode.UNAUTHORIZED.getStatus());
        assertThat(response.getContentType()).startsWith("application/json");
        assertThat(response.getContentAsString()).contains(ErrorCode.UNAUTHORIZED.getMessage());
    }
}
