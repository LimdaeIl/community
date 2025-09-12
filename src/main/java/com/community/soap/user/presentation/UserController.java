package com.community.soap.user.presentation;

import com.community.soap.common.aop.Permission;
import com.community.soap.common.resolver.CurrentUser;
import com.community.soap.common.resolver.CurrentUserInfo;
import com.community.soap.user.application.port.in.UserUseCase;
import com.community.soap.user.application.response.MyPageResponse;
import com.community.soap.user.domain.entity.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@RestController
public class UserController {

    private final UserUseCase userUseCase;

    @GetMapping("/me")
    @Permission(value = {UserRole.ADMIN, UserRole.MANAGER, UserRole.USER})
    public ResponseEntity<MyPageResponse> me(
            @CurrentUser CurrentUserInfo info
    ) {
        MyPageResponse response = userUseCase.me(info);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @Permission(value = {UserRole.ADMIN, UserRole.MANAGER, UserRole.USER})
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMe(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @CurrentUser CurrentUserInfo info
    ) {
        userUseCase.deleteMe(authorization, info.userId());

        return ResponseEntity
                .noContent()
                .build();
    }
}
