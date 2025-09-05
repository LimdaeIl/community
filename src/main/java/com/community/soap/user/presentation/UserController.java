package com.community.soap.user.presentation;

import com.community.soap.common.aop.Permission;
import com.community.soap.common.resolver.CurrentUser;
import com.community.soap.common.resolver.CurrentUserInfo;
import com.community.soap.user.application.UserUseCase;
import com.community.soap.user.application.response.MyPageResponse;
import com.community.soap.user.domain.entity.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@RestController
public class UserController {

    private final UserUseCase userUseCase;

    @GetMapping("/{userId}")
    @Permission(value = {UserRole.ADMIN, UserRole.MANAGER, UserRole.USER})
    public ResponseEntity<MyPageResponse> me(
            @PathVariable(name = "userId") Long userId
    ) {
        MyPageResponse response = userUseCase.me(userId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/admin/only")
    @Permission(UserRole.ADMIN) // ADMIN만 허용
    public ResponseEntity<String> adminOnly(
            @CurrentUser CurrentUserInfo currentUser
    ) {
        return ResponseEntity.ok("ok" + currentUser.userId());
    }


    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable(name = "userId") Long userId
    ) {
        userUseCase.deleteUser(userId);

        return ResponseEntity
                .noContent()
                .build();
    }
}
