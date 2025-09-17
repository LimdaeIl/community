package com.community.soap.catalog.application.response.category;

import com.community.soap.catalog.domain.entity.Category;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record CreateCategoryResponse(
        Long categoryId,
        String name,
        String description,
        Boolean isDeleted,
        LocalDateTime createdAt,
        Long createdBy,
        LocalDateTime updatedAt,
        Long updatedBy
) {

    public static CreateCategoryResponse from(Category category) {
        return CreateCategoryResponse.builder()
                .categoryId(category.getCategoryId())
                .name(category.getName())
                .description(category.getDescription())
                .isDeleted(category.getIsDeleted())
                .createdAt(category.getCreatedAt())
                .createdBy(category.getCreatedBy())
                .updatedAt(category.getUpdatedAt())
                .updatedBy(category.getUpdatedBy())
                .build();
    }
}
