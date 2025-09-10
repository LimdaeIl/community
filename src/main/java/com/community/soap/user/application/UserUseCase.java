package com.community.soap.user.application;

import com.community.soap.user.application.request.SignInRequest;
import com.community.soap.user.application.request.SignUpRequest;
import com.community.soap.user.application.response.MyPageResponse;
import com.community.soap.user.application.response.SignInResponse;
import com.community.soap.user.application.response.SignUpResponse;

public interface UserUseCase {

    SignUpResponse signup(SignUpRequest request);

    SignInResponse signIn(SignInRequest request);

    MyPageResponse me(Long userId);

    void deleteUser(Long userId);

    void logout(String authorizationHeader, String refreshToken, Long userIdFromCtx);

    SignInResponse refresh(String refreshToken);
}
