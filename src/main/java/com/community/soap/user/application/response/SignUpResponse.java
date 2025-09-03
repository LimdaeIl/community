package com.community.soap.user.application.response;

import com.community.soap.user.domain.entity.User;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record SignUpResponse(
        Long userId,
        String email,
        String nickname,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static SignUpResponse from(User register) {
        return SignUpResponse.builder()
                .userId(register.getUserId())
                .email(register.getEmail())
                .nickname(register.getNickname())
                .createdAt(register.getCreatedAt())
                .updatedAt(register.getUpdatedAt())
                .build();
    }
}
