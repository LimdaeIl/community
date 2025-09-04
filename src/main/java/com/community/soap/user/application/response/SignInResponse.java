package com.community.soap.user.application.response;

import com.community.soap.user.domain.entity.User;
import com.community.soap.user.domain.entity.UserRole;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record SignInResponse(
        Long userId,
        String email,
        String nickname,
        UserRole userRole,
        String tokenType,
        String accessToken,
        long accessTokenExpiresIn,   // ms
        String refreshToken,
        long refreshTokenExpiresIn   // ms
) {

    public static SignInResponse of(User user,
            String accessToken, long accessTtlMs,
            String refreshToken, long refreshTtlMs) {
        return new SignInResponse(
                user.getUserId(),
                user.getEmail(),
                user.getNickname(),
                user.getUserRole(),
                "Bearer",
                accessToken,
                accessTtlMs,
                refreshToken,
                refreshTtlMs
        );
    }
}