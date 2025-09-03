package com.community.soap.user.application.response;

import com.community.soap.user.domain.entity.User;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record SignInResponse(
        String email,
        String nickname,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static SignInResponse from(User byEmail) {
        return SignInResponse.builder()
                .email(byEmail.getEmail())
                .nickname(byEmail.getNickname())
                .createdAt(byEmail.getCreatedAt())
                .updatedAt(byEmail.getUpdatedAt())
                .build();
    }
}
