package com.community.soap.user.application.response;

import com.community.soap.user.domain.entity.User;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record MyPageResponse(
        String email,
        String nickname,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static MyPageResponse from(User byUserId) {
        return MyPageResponse.builder()
                .email(byUserId.getEmail())
                .nickname(byUserId.getNickname())
                .createdAt(byUserId.getCreatedAt())
                .updatedAt(byUserId.getUpdatedAt())
                .build();
    }
}
