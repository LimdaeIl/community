package com.community.soap.user.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter(AccessLevel.PUBLIC)
@Table(name = "s_user")
@Entity
public class User {

    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "password", nullable = false, length = 512)
    private String password;

    @Column(name = "nickname", nullable = false, length = 12)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false)
    private UserRole userRole;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = Boolean.FALSE;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    private User(Long userId, String email, String password, String nickname) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.userRole = UserRole.USER;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = null;
    }

    public static User register(Long userId, String email, String password, String nickname) {
        return new User(userId, email, password, nickname);
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
        update(userId);
    }

    public void softDelete(Long userId) {
        this.isDeleted = Boolean.TRUE;
        update(userId);
    }

    private void update(Long userId) {
        this.updatedBy = userId;
        this.updatedAt = LocalDateTime.now();
    }
}
