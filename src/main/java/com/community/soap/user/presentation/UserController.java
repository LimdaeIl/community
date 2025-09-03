package com.community.soap.user.presentation;

import com.community.soap.user.application.UserUseCase;
import com.community.soap.user.application.request.SignInRequest;
import com.community.soap.user.application.request.SignUpRequest;
import com.community.soap.user.application.response.MyPageResponse;
import com.community.soap.user.application.response.SignInResponse;
import com.community.soap.user.application.response.SignUpResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
public class UserController {

    private final UserUseCase userUseCase;

    @PostMapping("/auth/signup")
    public ResponseEntity<SignUpResponse> signup(@RequestBody @Valid SignUpRequest request) {
        SignUpResponse response = userUseCase.signup(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/auth/sign-in")
    public ResponseEntity<SignInResponse> signIn(
            @RequestBody @Valid SignInRequest request
    ) {
        SignInResponse response = userUseCase.signIn(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<MyPageResponse> myPage(
            @PathVariable(name = "userId") Long userId
    ) {
        MyPageResponse response = userUseCase.myPage(userId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable(name = "userId") Long userId
    ) {
        userUseCase.deleteUser(userId);

        return ResponseEntity
                .noContent()
                .build();
    }
}
