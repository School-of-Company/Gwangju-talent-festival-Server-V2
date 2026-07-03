package team.startup.gwangjutalentfestival.global.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import team.startup.gwangjutalentfestival.domain.auth.presentation.data.response.TokenResponse;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

/**
 * JWT 토큰 생성, 검증, 파싱을 담당하는 컴포넌트.
 * <p>액세스 토큰과 리프레시 토큰 발급, 클레임 추출, 블랙리스트 확인 전 토큰 유효성 검사를 수행한다.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtProperties jwtProperties;
    private SecretKey secretKey;

    private static final String TOKEN_TYPE = "type";
    private static final String ACCESS_TOKEN = "accessToken";
    private static final String REFRESH_TOKEN = "refreshToken";
    private static final String ROLE = "role";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 액세스 토큰과 리프레시 토큰을 함께 발급한다.
     *
     * @param userId 사용자 ID
     * @param role   사용자 역할
     * @return 발급된 토큰 정보 응답
     */
    public TokenResponse receiveToken(Long userId, Role role) {
        Date accessExpiryDate = calculateExpiryDate(jwtProperties.getAccessTokenExpiration());
        Date refreshExpiryDate = calculateExpiryDate(jwtProperties.getRefreshTokenExpiration());

        String accessToken = createToken(userId, role, ACCESS_TOKEN, accessExpiryDate);
        String refreshToken = createToken(userId, role, REFRESH_TOKEN, refreshExpiryDate);

        return new TokenResponse(
                accessToken,
                toLocalDateTime(accessExpiryDate),
                refreshToken,
                toLocalDateTime(refreshExpiryDate),
                role
        );
    }

    /**
     * 액세스 토큰을 단독으로 발급한다.
     *
     * @param userId 사용자 ID
     * @param role   사용자 역할
     * @return 액세스 토큰 문자열
     */
    public String generateAccessToken(Long userId, Role role) {
        Date expiryDate = calculateExpiryDate(jwtProperties.getAccessTokenExpiration());
        return createToken(userId, role, ACCESS_TOKEN, expiryDate);
    }

    /**
     * 리프레시 토큰을 단독으로 발급한다.
     *
     * @param userId 사용자 ID
     * @param role   사용자 역할
     * @return 리프레시 토큰 문자열
     */
    public String generateRefreshToken(Long userId, Role role) {
        Date expiryDate = calculateExpiryDate(jwtProperties.getRefreshTokenExpiration());
        return createToken(userId, role, REFRESH_TOKEN, expiryDate);
    }

    private String createToken(Long userId, Role role, String type, Date expiryDate) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim(ROLE, role.name())
                .claim(TOKEN_TYPE, type)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * JWT 토큰의 서명과 만료 여부를 검증한다.
     *
     * @param token 검증할 JWT 문자열
     * @return 유효하면 {@code true}, 그렇지 않으면 {@code false}
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.error("지원하지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    /**
     * JWT 토큰에서 클레임을 추출한다.
     *
     * @param token JWT 문자열
     * @return 파싱된 {@link Claims}
     */
    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 클레임의 토큰 타입이 액세스 토큰인지 확인한다.
     *
     * @param claims JWT 클레임
     * @return 액세스 토큰이면 {@code true}
     */
    public boolean isAccessToken(Claims claims) {
        return ACCESS_TOKEN.equals(claims.get(TOKEN_TYPE, String.class));
    }

    /**
     * 클레임의 토큰 타입이 리프레시 토큰인지 확인한다.
     *
     * @param claims JWT 클레임
     * @return 리프레시 토큰이면 {@code true}
     */
    public boolean isRefreshToken(Claims claims) {
        return REFRESH_TOKEN.equals(claims.get(TOKEN_TYPE, String.class));
    }

    /**
     * 클레임에서 사용자 ID를 추출한다.
     *
     * @param claims JWT 클레임
     * @return 사용자 ID
     */
    public Long getUserId(Claims claims) {
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 클레임에서 사용자 역할 문자열을 추출한다.
     *
     * @param claims JWT 클레임
     * @return 역할 문자열 (예: "ADMIN", "USER")
     */
    public String getRole(Claims claims) {
        return claims.get("role", String.class);
    }

    /**
     * Authorization 헤더 문자열에서 Bearer 토큰을 추출한다.
     *
     * @param authorizationHeader Authorization 헤더 값
     * @return 토큰 문자열, 형식이 맞지 않으면 {@code null}
     */
    public String extractBearerToken(String authorizationHeader) {
        return extractTokenFromHeader(authorizationHeader);
    }

    /**
     * HTTP 요청의 Authorization 헤더에서 Bearer 토큰을 추출한다.
     *
     * @param request HTTP 서블릿 요청
     * @return 토큰 문자열, 헤더가 없거나 형식이 맞지 않으면 {@code null}
     */
    public String resolveToken(HttpServletRequest request) {
        return extractTokenFromHeader(request.getHeader(AUTHORIZATION_HEADER));
    }

    private String extractTokenFromHeader(String bearerToken) {
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    /**
     * 토큰의 남은 만료 시간을 밀리초로 반환한다.
     *
     * @param claims JWT 클레임
     * @return 남은 만료 시간 (밀리초)
     */
    public long getRemainingExpireTime(Claims claims) {
        return claims.getExpiration().getTime() - System.currentTimeMillis();
    }

    /**
     * 만료된 토큰에서도 클레임을 추출한다.
     * <p>리프레시 토큰 재발급 등 만료 이후에도 클레임이 필요한 경우에 사용한다.</p>
     *
     * @param token JWT 문자열 (만료 여부 무관)
     * @return 파싱된 {@link Claims}
     */
    public Claims getClaimsAllowExpired(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    private LocalDateTime toLocalDateTime(Date date) {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    private Date calculateExpiryDate(long validitySeconds) {
        return new Date(System.currentTimeMillis() + validitySeconds * 1000L);
    }
}
