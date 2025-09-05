package com.community.soap.common.resolver;


import com.community.soap.user.domain.entity.UserRole;

public record CurrentUserInfo(
        Long userId,
        UserRole userRole
) {

    public static CurrentUserInfo of(Long userId, UserRole userRole) {
        return new CurrentUserInfo(userId, userRole);
    }
}
