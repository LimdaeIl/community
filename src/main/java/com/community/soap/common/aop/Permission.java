package com.community.soap.common.aop;

import com.community.soap.user.domain.entity.UserRole;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Permission {
    UserRole[] value() default {}; // 기본은 "아무 역할도 허용 X"가 아니라 "명시적으로만 체크" 용도
}
