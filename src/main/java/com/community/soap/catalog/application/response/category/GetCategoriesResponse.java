package com.community.soap.catalog.application.response.category;

import com.community.soap.catalog.domain.entity.Category;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record GetCategoriesResponse(
        Long categoryId,
        String name,
        String description,
        LocalDateTime createdAt,
        Long createdBy,
        LocalDateTime updatedAt,
        Long updatedBy
) {

    public static GetCategoriesResponse from(Category category) {
        return GetCategoriesResponse.builder()
                .categoryId(category.getCategoryId())
                .name(category.getName())
                .description(category.getDescription())
                .createdAt(category.getCreatedAt())
                .createdBy(category.getCreatedBy())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

}
