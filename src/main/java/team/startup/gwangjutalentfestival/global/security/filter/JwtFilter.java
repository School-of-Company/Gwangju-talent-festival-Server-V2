package team.startup.gwangjutalentfestival.global.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import team.startup.gwangjutalentfestival.domain.auth.repository.TokenBlacklistRepository;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;
import team.startup.gwangjutalentfestival.global.auth.CustomUserDetails;
import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.ErrorResponse;
import team.startup.gwangjutalentfestival.global.jwt.JwtProvider;
import team.startup.gwangjutalentfestival.global.security.config.PublicEndpointMatcher;

import java.io.IOException;

/**
 * 요청당 한 번 실행되는 JWT 인증 필터.
 * <p>Authorization 헤더에서 액세스 토큰을 추출하여 유효성 검증 후 {@link org.springframework.security.core.context.SecurityContext}에 인증 정보를 등록한다.
 * 블랙리스트에 등록된 토큰은 거부한다.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final ObjectMapper objectMapper;
    private final JwtProvider jwtProvider;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return PublicEndpointMatcher.matcher().matches(request);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String token = jwtProvider.resolveToken(request);

        if (token != null) {
            if (!jwtProvider.validateToken(token)) {
                setErrorResponse(response, ErrorCode.INVALID_TOKEN);
                return;
            }
            try {
                Claims claims = jwtProvider.getClaims(token);
                String jti = claims.getId();
                if (!jwtProvider.isAccessToken(claims) || (jti != null && tokenBlacklistRepository.isBlacklisted(jti))) {
                    setErrorResponse(response, ErrorCode.INVALID_TOKEN);
                    return;
                }
                Long userId = jwtProvider.getUserId(claims);
                Role role = Role.valueOf(jwtProvider.getRole(claims));
                CustomUserDetails userDetails = CustomUserDetails.fromToken(userId, role);
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException e) {
                setErrorResponse(response, ErrorCode.INVALID_TOKEN);
                return;
            } catch (Exception e) {
                log.error("인증 처리 중 오류 발생", e);
                setErrorResponse(response, ErrorCode.INVALID_TOKEN);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void setErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getStatus());
        response.setContentType("application/json; charset=UTF-8");
        objectMapper.writeValue(
                response.getWriter(),
                new ErrorResponse(errorCode.getStatus(), errorCode.getMessage())
        );
    }
}
