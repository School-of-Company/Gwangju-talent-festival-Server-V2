package team.startup.gwangjutalentfestival.global.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import team.startup.gwangjutalentfestival.domain.auth.repository.TokenBlacklistRepository;
import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.jwt.JwtProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @Mock
    private TokenBlacklistRepository tokenBlacklistRepository;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private FilterChain filterChain;

    private JwtFilter jwtFilter;

    @BeforeEach
    void setUp() {
        jwtFilter = new JwtFilter(tokenBlacklistRepository, new ObjectMapper(), jwtProvider);
    }

    @Test
    void 공개_API는_Authorization_헤더가_있어도_JWT_검증을_건너뛴다() throws Exception {
        MockHttpServletRequest request = request("POST", "/slogan");
        request.addHeader("Authorization", "Bearer invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtFilter.doFilter(request, response, filterChain);

        verify(jwtProvider, never()).resolveToken(request);
        verify(filterChain).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void 공개_GET_API는_Authorization_헤더가_있어도_JWT_검증을_건너뛴다() throws Exception {
        MockHttpServletRequest request = request("GET", "/team");
        request.setQueryString("sort=order");
        request.addHeader("Authorization", "Bearer invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtFilter.doFilter(request, response, filterChain);

        verify(jwtProvider, never()).resolveToken(request);
        verify(filterChain).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void 인증이_필요한_API는_잘못된_JWT를_검증하고_거부한다() throws Exception {
        MockHttpServletRequest request = request("GET", "/seat");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(jwtProvider.resolveToken(request)).thenReturn("invalid-token");
        when(jwtProvider.validateToken("invalid-token")).thenReturn(false);

        jwtFilter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(ErrorCode.INVALID_TOKEN.getStatus());
        assertThat(response.getContentAsString()).contains(ErrorCode.INVALID_TOKEN.getMessage());
    }

    @Test
    void 인증이_필요한_API에_토큰이_없으면_필터를_통과한다() throws Exception {
        MockHttpServletRequest request = request("GET", "/seat");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(jwtProvider.resolveToken(request)).thenReturn(null);

        jwtFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    private MockHttpServletRequest request(String method, String path) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setServletPath(path);
        return request;
    }
}
