package com.community.soap.common.aop;

import static com.community.soap.common.util.AuthKeys.ATTR_USER_ROLE;

import com.community.soap.common.exception.AppException;
import com.community.soap.common.exception.CommonErrorCode;
import com.community.soap.user.domain.entity.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


@Slf4j(topic = "PermissionAspect")
@Aspect
@Component
public class PermissionAspect {

    @Before("@within(com.community.soap.common.aop.Permission) || @annotation(com.community.soap.common.aop.Permission)")
    public void permission(JoinPoint jp) {
        Permission perm = resolvePermission(jp);
        if (perm == null) return; // 애너테이션이 아예 없으면 권한 체크 생략

        UserRole current = currentUserRoleOr401();

        UserRole[] required = perm.value();
        if (required.length == 0) {
            // 애너테이션은 있으나 값이 비어있으면 "로그인만 강제"로 처리 (403 체크 X)
            return;
        }

        Set<UserRole> allowed = Arrays.stream(required).collect(Collectors.toSet());
        if (!allowed.contains(current)) {
            log.info("권한 거부: 현재 역할={}, 허용 역할={}", current, allowed);
            throw new AppException(CommonErrorCode.FORBIDDEN); // 403
        }
    }

    private Permission resolvePermission(JoinPoint jp) {
        MethodSignature sig = (MethodSignature) jp.getSignature();
        Permission method = sig.getMethod().getAnnotation(Permission.class);
        if (method != null) return method;
        return jp.getTarget().getClass().getAnnotation(Permission.class);
    }

    private UserRole currentUserRoleOr401() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) throw new AppException(CommonErrorCode.UNAUTHORIZED);
        HttpServletRequest req = attrs.getRequest();

        Object roleAttr = req.getAttribute(ATTR_USER_ROLE);
        if (!(roleAttr instanceof UserRole role)) {
            // 토큰 없음/무효 등으로 역할이 없는 경우
            throw new AppException(CommonErrorCode.UNAUTHORIZED);
        }
        return role;
    }
}
