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

    public String generateAccessToken(Long userId, Role role) {
        Date expiryDate = calculateExpiryDate(jwtProperties.getAccessTokenExpiration());
        return createToken(userId, role, ACCESS_TOKEN, expiryDate);
    }

    public String generateRefreshToken(Long userId, Role role) {
        Date expiryDate = calculateExpiryDate(jwtProperties.getRefreshTokenExpiration());
        return createToken(userId, role, REFRESH_TOKEN, expiryDate);
    }

    private String createToken(Long userId, Role role, String type, Date expiryDate) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim(ROLE, role.name())
                .claim(TOKEN_TYPE, type)
                .setId(UUID.randomUUID().toString())  // 추가
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

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

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isAccessToken(Claims claims) {
        return ACCESS_TOKEN.equals(claims.get(TOKEN_TYPE, String.class));
    }

    public boolean isRefreshToken(Claims claims) {
        return REFRESH_TOKEN.equals(claims.get(TOKEN_TYPE, String.class));
    }

    public Long getUserId(Claims claims) {
        return Long.parseLong(claims.getSubject());
    }

    public String getRole(Claims claims) {
        return claims.get("role", String.class);
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    public long getRemainingExpireTime(Claims claims) {
        return claims.getExpiration().getTime() - System.currentTimeMillis();
    }

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
