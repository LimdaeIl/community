package com.community.soap.catalog.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "s_category")
@Entity
public class Category {

    @Id
    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    // TODO: 트리 구조는 나중에. 지금은 단일 계층만.
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = Boolean.FALSE;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    private Category(Long categoryId, String name, String description, Long createdBy) {
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.isDeleted = Boolean.FALSE;
        this.createdAt = LocalDateTime.now();
        this.createdBy = createdBy;
        this.updatedAt = null;
        this.updatedBy = null;
    }

    public static Category of(Long categoryId, String name, String description, Long createdBy) {
        return new Category(categoryId, name, description, createdBy);
    }

    public void updateName(Long userId, String newName) {
        this.name = newName;
        update(userId);
    }

    public void updateDescription(Long userId, String newDescription) {
        this.description = newDescription;
        update(userId);
    }

    public void updateIsDeleted(Long userId, Boolean isDeleted) {
        this.isDeleted = isDeleted;
        update(userId);
    }

    private void update(Long userId) {
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = userId;
    }
}
