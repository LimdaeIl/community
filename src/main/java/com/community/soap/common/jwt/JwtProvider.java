package com.community.soap.common.jwt;

import com.community.soap.user.domain.entity.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.time.Duration;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtProvider {

    // ====== CLAIM & HEADER KEYS ======
    private static final String CLAIM_USER_ROLE = "USER_ROLE";
    private static final String PREFIX_BEARER = "Bearer ";
    private static final long DEFAULT_CLOCK_SKEW_SECONDS = 120; // 2분 허용

    // ====== KEYS ======
    private final SecretKey accessTokenKey;
    private final SecretKey refreshTokenKey;

    // ====== EXPIRATIONS (millis) ======
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtProvider(
            @Value("${spring.jwt.secret-access}") String accessSecretBase64,
            @Value("${spring.jwt.secret-refresh}") String refreshSecretBase64,
            @Value("${spring.jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${spring.jwt.refresh-token-expiration}") long refreshTokenExpiration
    ) {
        // 키는 Base64 인코딩된 문자열을 권장 (256bit 이상)
        this.accessTokenKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(Objects.requireNonNull(accessSecretBase64)));
        this.refreshTokenKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(Objects.requireNonNull(refreshSecretBase64)));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    // ====== GENERATE ======
    public String generateAccessToken(Long userId, UserRole userRole) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .header().type("JWT").and()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(exp)
                .claim(CLAIM_USER_ROLE, userRole.name())
                .id(UUID.randomUUID().toString())   // jti: 블랙리스트/로테이션에 유용
                .signWith(accessTokenKey, Jwts.SIG.HS256)
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
                .header().type("JWT").and()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(exp)
                .id(UUID.randomUUID().toString())
                .signWith(refreshTokenKey, Jwts.SIG.HS256)
                .compact();
    }

    // ====== EXTRACT ======
    public Long getUserId(String tokenOrBearer) {
        Claims claims = parseAccessClaims(tokenOrBearer);
        return Long.parseLong(claims.getSubject());
    }

    public String getUserRole(String tokenOrBearer) {
        Claims claims = parseAccessClaims(tokenOrBearer);
        Object role = claims.get(CLAIM_USER_ROLE);
        if (role == null) {
            throw new TokenException(JwtErrorCode.MALFORMED_TOKEN); // 혹은 적절한 코드
        }
        return role.toString();
    }

    /**
     * 만료까지 남은 시간(ms). 만료 시 0
     */
    public long getRemainingTimeMillis(String tokenOrBearer) {
        Claims claims = parseAccessClaims(tokenOrBearer);
        long remaining = claims.getExpiration().getTime() - System.currentTimeMillis();
        return Math.max(remaining, 0);
    }

    /**
     * 리프레시 토큰에서 userId 추출 (필요 시)
     */
    public Long getUserIdFromRefresh(String tokenOrBearer) {
        Claims claims = parseRefreshClaims(tokenOrBearer);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * jti 추출: 블랙리스트 키로 활용 가능
     */
    public String getJti(String tokenOrBearer) {
        Claims claims = parseAccessClaims(tokenOrBearer);
        return claims.getId();
    }

    // ====== VALIDATE (예: 컨트롤러/필터에서 빠른 검증 용도) ======
    public boolean isAccessTokenValid(String tokenOrBearer) {
        try {
            parseAccessClaims(tokenOrBearer);
            return true;
        } catch (TokenException e) {
            return false;
        }
    }

    // ====== INTERNAL PARSERS ======
    private Claims parseAccessClaims(String tokenOrBearer) {
        String token = stripBearer(tokenOrBearer);
        try {
            return Jwts.parser()
                    .verifyWith(accessTokenKey)
                    .clockSkewSeconds(DEFAULT_CLOCK_SKEW_SECONDS)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new TokenException(JwtErrorCode.EXPIRED_TOKEN);
        } catch (MalformedJwtException e) {
            throw new TokenException(JwtErrorCode.MALFORMED_TOKEN);
        } catch (SecurityException | SignatureException e) {
            // 0.12.x에선 SecurityException도 서명 관련으로 올 수 있음
            throw new TokenException(JwtErrorCode.TAMPERED_TOKEN);
        } catch (JwtException e) {
            log.debug("JWT parse error: {}", e.toString()); // 내부 디버깅용
            throw new TokenException(JwtErrorCode.INVALID_BEARER_TOKEN);
        }
    }

    private Claims parseRefreshClaims(String tokenOrBearer) {
        String token = stripBearer(tokenOrBearer);
        try {
            return Jwts.parser()
                    .verifyWith(refreshTokenKey)
                    .clockSkewSeconds(DEFAULT_CLOCK_SKEW_SECONDS)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new TokenException(JwtErrorCode.EXPIRED_TOKEN);
        } catch (MalformedJwtException e) {
            throw new TokenException(JwtErrorCode.MALFORMED_TOKEN);
        } catch (SecurityException | SignatureException e) {
            throw new TokenException(JwtErrorCode.TAMPERED_TOKEN);
        } catch (JwtException e) {
            log.debug("JWT parse error (refresh): {}", e.toString());
            throw new TokenException(JwtErrorCode.INVALID_BEARER_TOKEN);
        }
    }

    private String stripBearer(String tokenOrBearer) {
        if (tokenOrBearer == null || tokenOrBearer.isBlank()) {
            throw new TokenException(JwtErrorCode.INVALID_BEARER_TOKEN);
        }
        String t = tokenOrBearer.trim();
        if (t.regionMatches(true, 0, PREFIX_BEARER, 0, PREFIX_BEARER.length())) {
            return t.substring(PREFIX_BEARER.length()).trim();
        }
        return t;
    }

    // ====== 편의 메서드 (블랙리스트 TTL 용) ======
    public Duration accessTokenTtlOf(String tokenOrBearer) {
        return Duration.ofMillis(getRemainingTimeMillis(tokenOrBearer));
    }

    // Get 리프레시 토큰 By Jti
    public String getRefreshJti(String tokenOrBearer) {
        Claims claims = parseRefreshClaims(tokenOrBearer);
        return claims.getId();
    }

    // 리프레시 토큰의 jti/남은 시간
    public Duration refreshTokenTtlOf(String tokenOrBearer) {
        Claims claims = parseRefreshClaims(tokenOrBearer);
        long remaining = claims.getExpiration().getTime() - System.currentTimeMillis();
        return Duration.ofMillis(Math.max(remaining, 0));
    }
}

