package com.community.soap.user.application.response;

public record EmailVerificationCodeResponse(
        String email,
        long expireInMs
) {
    public static EmailVerificationCodeResponse of(String email, long expireInMs) {
        return new EmailVerificationCodeResponse(email, expireInMs);
    }
}
