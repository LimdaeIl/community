package com.community.soap.user.presentation;

import com.community.soap.user.application.UserUseCase;
import com.community.soap.user.application.request.LogoutRequest;
import com.community.soap.user.application.request.RefreshRequest;
import com.community.soap.user.application.request.SignInRequest;
import com.community.soap.user.application.request.SignUpRequest;
import com.community.soap.user.application.response.LogoutResponse;
import com.community.soap.user.application.response.SignInResponse;
import com.community.soap.user.application.response.SignUpResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@RestController
public class AuthController {

    private final UserUseCase userUseCase;

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signup(@RequestBody @Valid SignUpRequest request) {
        SignUpResponse response = userUseCase.signup(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<SignInResponse> signIn(
            @RequestBody @Valid SignInRequest request
    ) {
        SignInResponse response = userUseCase.signIn(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @RequestHeader(name = "X-User-Id") Long userId, // 프로젝트 방식에 맞게 (예: 게이트웨이에서 주입)
            @RequestBody LogoutRequest request
    ) {
        userUseCase.logout(authorization, request.refreshToken(), userId);
        return ResponseEntity.ok(LogoutResponse.ok());
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<SignInResponse> refresh(@RequestBody RefreshRequest request) {
        return ResponseEntity.ok(userUseCase.refresh(request.refreshToken()));
    }
}
