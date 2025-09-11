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

/**
 * - excludePaths, excludeMethods 기반으로 인증 제외
 * - Authorization 헤더의 Bearer 토큰 또는 쿠키에서 액세스 토큰 추출
 * - 토큰에서 userId/role 파싱 후 request attribute 로 저장
 * - 예외는 던지고, 상위 ExceptionHandlingFilter 가 처리
 */
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

    /**
     * "/**"의 base, 트레일링 슬래시 제거 변형을 함께 등록해 오탐/미탐을 방지
     */
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
        // 1) 메서드 제외 (기본 OPTIONS)
        if (props.getExcludeMethods().stream()
                .anyMatch(m -> m.equalsIgnoreCase(request.getMethod()))) {
            return true;
        }
        // 2) 경로 제외
        String stripped = stripContextPath(request.getRequestURI(), request.getContextPath());
        PathContainer container = PathContainer.parsePath(stripped);
        return excludePatterns.stream().anyMatch(p -> p.matches(container));
    }

    private String stripContextPath(String uri, String ctx) {
        if (!StringUtils.hasLength(ctx)) {
            return uri;
        }
        return uri.startsWith(ctx) ? uri.substring(ctx.length()) : uri;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 여기까지 왔다는 건 shouldNotFilter=false → 인증 필요
        String accessToken = resolveAccessToken(request);
        if (!StringUtils.hasText(accessToken)) {
            throw new AppException(CommonErrorCode.INVALID_HEADER); // Authorization/Cookie 둘 다 없음
        }

        try {
            Long userId = jwtProvider.getUserId(accessToken);
            String roleStr = jwtProvider.getUserRole(accessToken);

            if (userId == null || !StringUtils.hasText(roleStr)) {
                throw new AppException(CommonErrorCode.INVALID_TOKEN);
            }

            UserRole role;
            try {
                role = UserRole.valueOf(roleStr.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                throw new AppException(CommonErrorCode.INVALID_ROLE_BY_TOKEN);
            }

            // 컨트롤러/리졸버/Aspect에서 공통으로 쓰는 키로 저장
            request.setAttribute(ATTR_USER_ID, userId);
            request.setAttribute(ATTR_USER_ROLE, role);

        } catch (AppException e) {
            // AppException은 그대로 전파 (ExceptionHandlingFilter가 응답 작성)
            throw e;
        } catch (Exception e) {
            // 그 외는 일반 토큰 오류로 통일
            throw new AppException(CommonErrorCode.INVALID_TOKEN);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Authorization 헤더(Bearer …) 우선, 없으면 쿠키(props.accessTokenCookie)에서 조회. "Bearer" 접두사는 대소문자 무시 +
     * 앞뒤 공백 허용.
     */
    private String resolveAccessToken(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (StringUtils.hasText(auth)) {
            String candidate = auth.trim();
            // "Bearer " 접두어 대소문자 무시
            if (candidate.regionMatches(true, 0, "Bearer ", 0, 7)) {
                return candidate.substring(7).trim();
            }
            // 접두어가 없으면 아예 토큰으로 보지 않고 INVALID_TOKEN 처리하도록 null 반환
            return null;
        }

        // 헤더가 없다면 쿠키 fallback
        String cookieName = props.getAccessTokenCookie();
        if (request.getCookies() != null && StringUtils.hasText(cookieName)) {
            for (Cookie c : request.getCookies()) {
                if (cookieName.equals(c.getName()) && StringUtils.hasText(c.getValue())) {
                    return c.getValue().trim();
                }
            }
        }
        return null;
    }
}
