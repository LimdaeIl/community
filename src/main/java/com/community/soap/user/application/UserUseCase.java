package com.community.soap.user.application;

import com.community.soap.common.resolver.CurrentUserInfo;
import com.community.soap.user.application.request.EmailVerificationCodeRequest;
import com.community.soap.user.application.request.EmailVerifyCodeRequest;
import com.community.soap.user.application.request.SignInRequest;
import com.community.soap.user.application.request.SignUpRequest;
import com.community.soap.user.application.response.EmailVerificationCodeResponse;
import com.community.soap.user.application.response.MyPageResponse;
import com.community.soap.user.application.response.SignInResponse;
import com.community.soap.user.application.response.SignUpResponse;
import jakarta.validation.Valid;

public interface UserUseCase {

    SignUpResponse signup(SignUpRequest request);

    SignInResponse signIn(SignInRequest request);

    MyPageResponse me(CurrentUserInfo info);

    void deleteUserAsAdmin(Long targetUserId);

    void deleteMe(String authorizationHeader, Long userIdFromCtx);

    void logout(String authorizationHeader, String refreshToken, Long userIdFromCtx);

    SignInResponse refresh(String refreshToken);

    EmailVerificationCodeResponse emailVerificationCode(EmailVerificationCodeRequest request);

    void emailVerifyCode(EmailVerifyCodeRequest request);
}
