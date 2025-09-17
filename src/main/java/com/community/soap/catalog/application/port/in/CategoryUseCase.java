package com.community.soap.catalog.application.port.in;

import com.community.soap.catalog.application.request.category.CreateCategoryRequest;
import com.community.soap.catalog.application.request.category.UpdateCategoryRequest;
import com.community.soap.catalog.application.response.category.CreateCategoryResponse;
import com.community.soap.catalog.application.response.category.GetCategoriesResponse;
import com.community.soap.catalog.application.response.category.UpdateCategoryResponse;
import com.community.soap.common.util.PageResponse;
import org.springframework.data.domain.Pageable;

public interface CategoryUseCase {

    CreateCategoryResponse createCategory(Long userId, CreateCategoryRequest request);

    PageResponse<GetCategoriesResponse> getCategories(String q, Pageable pageable, boolean all);

    UpdateCategoryResponse updateCategory(Long userId, Long categoryId, UpdateCategoryRequest request);

    void softDeleteCategory(Long userId, Long categoryId);
}
