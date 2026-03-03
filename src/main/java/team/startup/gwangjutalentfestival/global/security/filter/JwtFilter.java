package team.startup.gwangjutalentfestival.global.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import team.startup.gwangjutalentfestival.domain.user.enums.Role;
import team.startup.gwangjutalentfestival.global.auth.CustomUserDetails;
import team.startup.gwangjutalentfestival.global.exception.ErrorCode;
import team.startup.gwangjutalentfestival.global.exception.ErrorResponse;
import team.startup.gwangjutalentfestival.global.jwt.JwtProvider;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;
    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String token = jwtProvider.resolveToken(request);

        if (token != null) {
            try {
                if (jwtProvider.validateToken(token) && jwtProvider.isAccessToken(token)) {
                    Long userId = jwtProvider.getUserId(token);
                    Role role = jwtProvider.getRole(token);
                    String phoneNumber = jwtProvider.getPhoneNumber(token);

                    CustomUserDetails customUserDetails = CustomUserDetails.fromToken(userId, phoneNumber, role);
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            customUserDetails,
                            null,
                            customUserDetails.getAuthorities());

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    setErrorResponse(response, ErrorCode.INVALID_TOKEN);
                    return;
                }
            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                setErrorResponse(response, ErrorCode.EXPIRED_TOKEN);
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
