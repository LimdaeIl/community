package com.community.soap.common.resolver;

import com.community.soap.common.exception.AppException;
import com.community.soap.common.exception.CommonErrorCode;
import com.community.soap.common.util.AuthKeys;
import com.community.soap.user.domain.entity.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Slf4j(topic = "CurrentUserArgumentResolver")
@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && CurrentUserInfo.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);

        // 1) attribute 우선 (필터가 타입 세이프로 넣어준 경우)
        Object idAttr = request.getAttribute(AuthKeys.ATTR_USER_ID);
        Object roleAttr = request.getAttribute(AuthKeys.ATTR_USER_ROLE);

        Long userId = coerceUserId(idAttr);
        UserRole userRole = coerceUserRole(roleAttr);

        // 2) attribute가 없으면 header fallback
        if (userId == null) {
            userId = parseUserIdHeader(request.getHeader(AuthKeys.HDR_USER_ID));
        }
        if (userRole == null) {
            userRole = parseUserRoleHeader(request.getHeader(AuthKeys.HDR_USER_ROLE));
        }

        if (userId == null || userRole == null) {
            // 인증 정보 자체가 없음 → 401
            throw new AppException(CommonErrorCode.UNAUTHORIZED);
        }

        try {
            return CurrentUserInfo.of(userId, userRole);
        } catch (IllegalArgumentException e) {
            // 포맷 자체가 잘못됨 → 400
            throw new AppException(CommonErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private Long coerceUserId(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Long l) {
            return l;
        }
        if (v instanceof Integer i) {
            return i.longValue();
        }
        if (v instanceof String s) {
            try {
                return Long.parseLong(s.trim());
            } catch (NumberFormatException ignore) {
                return null;
            }
        }
        return null;
    }

    private UserRole coerceUserRole(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof UserRole r) {
            return r;
        }
        if (v instanceof String s) {
            return toUserRole(s);
        }
        return null;
    }

    private Long parseUserIdHeader(String headerVal) {
        if (headerVal == null || headerVal.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(headerVal.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private UserRole parseUserRoleHeader(String headerVal) {
        if (headerVal == null || headerVal.isBlank()) {
            return null;
        }
        return toUserRole(headerVal);
    }

    private UserRole toUserRole(String s) {
        try {
            return UserRole.valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            // 포맷 오류는 호출부에서 400 처리
            return null;
        }
    }
}