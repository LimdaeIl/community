package com.community.soap.common.filter;


import com.community.soap.common.exception.AppException;
import com.community.soap.common.exception.CommonErrorCode;
import com.community.soap.common.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * - 서블릿/필터 레이어에서 발생한 예외를 JSON 표준 응답으로 변환
 * - JwtAuthenticationFilter 같은 하위 필터가 던진 AppException을 여기서 마무리
 * - 컨트롤러 레이어 예외는 @RestControllerAdvice가 우선 처리하되, 바깥으로 나오면 이 필터가 최종 방어
 */
@Slf4j(topic = "ExceptionHandlingFilter")
@RequiredArgsConstructor
public class ExceptionHandlingFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        // 서블릿 ERROR 디스패치 단계에서 중복 처리 방지
        return true;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (AppException ex) {
            writeAppError(response, request, ex);
        } catch (Exception ex) {
            log.error("Unhandled exception", ex);
            writeGenericError(response, request);
        }
    }

    private void writeAppError(HttpServletResponse resp, HttpServletRequest req, AppException ex)
            throws IOException {
        if (resp.isCommitted()) {
            return;
        }

        ErrorCode code = ex.getErrorCode(); // 인터페이스여도 OK
        HttpStatus status = (code != null && code.getStatus() != null)
                ? code.getStatus()
                : HttpStatus.BAD_REQUEST;

        String message = (code != null && StringUtils.hasText(code.getMessage()))
                ? code.getMessage()
                : (StringUtils.hasText(ex.getMessage()) ? ex.getMessage()
                        : status.getReasonPhrase());

        logAtProperLevel(code, ex);

        String codeName = (code instanceof Enum<?> e) ? e.name()
                : (code != null ? code.getClass().getSimpleName() : "APP_ERROR");

        resp.resetBuffer();
        resp.setStatus(status.value());
        resp.setContentType("application/json;charset=UTF-8");

        Map<String, Object> body = Map.of(
                "timestamp", OffsetDateTime.now().toString(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "code", codeName,
                "message", message,
                "path", req.getRequestURI()
        );
        objectMapper.writeValue(resp.getWriter(), body);
        resp.flushBuffer();
    }

    private void writeGenericError(HttpServletResponse resp, HttpServletRequest req)
            throws IOException {
        if (resp.isCommitted()) {
            return;
        }

        HttpStatus status = CommonErrorCode.INTERNAL_SERVER_ERROR.getStatus();
        String message = CommonErrorCode.INTERNAL_SERVER_ERROR.getMessage();

        resp.resetBuffer();
        resp.setStatus(status.value());
        resp.setContentType("application/json;charset=UTF-8");

        Map<String, Object> body = Map.of(
                "timestamp", OffsetDateTime.now().toString(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "code", CommonErrorCode.INTERNAL_SERVER_ERROR.name(),
                "message", message,
                "path", req.getRequestURI()
        );
        objectMapper.writeValue(resp.getWriter(), body);
        resp.flushBuffer();
    }

    private void logAtProperLevel(ErrorCode code, AppException ex) {
        HttpStatus status = (code != null) ? code.getStatus() : null;
        if (status == null) {
            log.error("AppException without status", ex);
            return;
        }
        if (status.is4xxClientError()) {
            // 인증/권한/요청 오류 등 클라이언트계열 → warn
            log.warn("Handled AppException: {} ({})", codeName(code), status, ex);
        } else {
            // 서버 오류 → error
            log.error("Handled AppException: {} ({})", codeName(code), status, ex);
        }
    }

    private String codeName(ErrorCode code) {
        return (code instanceof Enum<?> e) ? e.name()
                : (code != null ? code.getClass().getSimpleName() : "APP_ERROR");
    }
}
