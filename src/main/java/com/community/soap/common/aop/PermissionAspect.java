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
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


@Slf4j(topic = "PermissionAspect")
@Aspect
@Component
public class PermissionAspect {

    @Before("@annotation(permission)")
    public void permission(JoinPoint joinPoint, Permission permission) {
        Set<UserRole> allowed = Arrays.stream(permission.value()).collect(Collectors.toSet());
        UserRole current = currentUserRoleOr401();

        if (!allowed.contains(current)) {
            log.info("권한 거부: 현재 역할={}, 허용 역할={}", current, allowed);
            throw new AppException(CommonErrorCode.FORBIDDEN);
        }
    }

    private UserRole currentUserRoleOr401() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            log.warn("ServletRequestAttributes is null");
            throw new AppException(CommonErrorCode.UNAUTHORIZED);
        }
        HttpServletRequest req = attrs.getRequest();

        Object roleAttr = req.getAttribute(ATTR_USER_ROLE);
        if (!(roleAttr instanceof UserRole role)) {
            log.warn("요청에 역할 attribute가 없습니다: {}", ATTR_USER_ROLE);
            throw new AppException(CommonErrorCode.UNAUTHORIZED);
        }
        return role;
    }
}
