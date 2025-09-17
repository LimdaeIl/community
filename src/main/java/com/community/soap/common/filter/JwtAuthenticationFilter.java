package com.community.soap.common.filter;

import static com.community.soap.common.util.AuthKeys.ATTR_USER_ID;
import static com.community.soap.common.util.AuthKeys.ATTR_USER_ROLE;

import com.community.soap.common.exception.AppException;
import com.community.soap.common.exception.CommonErrorCode;
import com.community.soap.common.jwt.JwtProvider;
import com.community.soap.user.domain.entity.UserRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.PathContainer;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

@Slf4j(topic = "JwtAuthenticationFilter")
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final JwtFilterProperties props;
    private final List<PathPattern> excludePatterns;
    private final PathPatternParser parser = PathPatternParser.defaultInstance;

    public JwtAuthenticationFilter(JwtProvider jwtProvider, JwtFilterProperties props) {
        this.jwtProvider = Objects.requireNonNull(jwtProvider);
        this.props = Objects.requireNonNull(props);
        this.excludePatterns = props.getExcludePaths().stream()
                .flatMap(this::expandPatternVariants)
                .map(parser::parse)
                .toList();
    }

    private Stream<String> expandPatternVariants(String raw) {
        Stream<String> s = Stream.of(raw);
        if (raw.endsWith("/**")) {
            s = Stream.concat(s, Stream.of(raw.substring(0, raw.length() - 3)));
        }
        if (raw.endsWith("/") && raw.length() > 1) {
            s = Stream.concat(s, Stream.of(raw.substring(0, raw.length() - 1)));
        }
        return s.distinct();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return false; // 항상 실행
    }

    private String stripContextPath(String uri, String ctx) {
        if (!StringUtils.hasLength(ctx)) {
            return uri;
        }
        return uri.startsWith(ctx) ? uri.substring(ctx.length()) : uri;
    }

    private boolean isExcluded(HttpServletRequest request) {
        if (props.getExcludeMethods().stream()
                .anyMatch(m -> m.equalsIgnoreCase(request.getMethod()))) {
            return true;
        }
        String stripped = stripContextPath(request.getRequestURI(), request.getContextPath());
        PathContainer container = PathContainer.parsePath(stripped);
        return excludePatterns.stream().anyMatch(p -> p.matches(container));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain)
            throws ServletException, IOException {

        boolean excluded = isExcluded(request);
        String accessToken = resolveAccessToken(request);

        if (!StringUtils.hasText(accessToken)) {
            if (excluded) { // 공개 경로: 토큰 없어도 통과
                chain.doFilter(request, response);
                return;
            }
            throw new AppException(CommonErrorCode.INVALID_HEADER); // 비공개 경로: 401
        }

        try {
            Long userId = jwtProvider.getUserId(accessToken);
            String roleStr = jwtProvider.getUserRole(accessToken);
            UserRole role = resolveRole(roleStr); // ← 여기서 ROLE_ 접두사/대소문자 정규화

            request.setAttribute(ATTR_USER_ID, userId);
            request.setAttribute(ATTR_USER_ROLE, role);

            if (log.isDebugEnabled()) {
                log.debug("JWT OK -> userId={}, role={}", userId, role);
            }
        } catch (AppException e) {
            if (excluded) { // 공개 경로: 토큰이 이상해도 인증 강요하지 않음(속성 미세팅 상태로 통과)
                chain.doFilter(request, response);
                return;
            }
            throw e; // 비공개 경로: 401
        }

        chain.doFilter(request, response);
    }

    private String resolveAccessToken(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (StringUtils.hasText(auth)) {
            String candidate = auth.trim();
            if (candidate.regionMatches(true, 0, "Bearer ", 0, 7)) {
                return candidate.substring(7).trim();
            }
            return null; // Bearer 접두사 없으면 무효로 간주
        }

        // 쿠키 폴백은 설정 켜졌을 때만
        if (props.isCookieFallbackEnabled()) {
            String cookieName = props.getAccessTokenCookie();
            if (request.getCookies() != null && StringUtils.hasText(cookieName)) {
                for (Cookie c : request.getCookies()) {
                    if (cookieName.equals(c.getName()) && StringUtils.hasText(c.getValue())) {
                        return c.getValue().trim();
                    }
                }
            }
        }
        return null;
    }

    // <-- 질문하신 이 부분!
    private UserRole resolveRole(String roleStr) {
        if (!StringUtils.hasText(roleStr)) {
            throw new AppException(CommonErrorCode.INVALID_TOKEN);
        }
        String normalized = roleStr.trim().toUpperCase(Locale.ROOT);
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring(5); // ROLE_ADMIN -> ADMIN
        }
        try {
            return UserRole.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new AppException(CommonErrorCode.INVALID_ROLE_BY_TOKEN);
        }
    }
}
