package com.community.soap.catalog.application.service;

import com.community.soap.catalog.application.port.in.CategoryUseCase;
import com.community.soap.catalog.application.port.out.CategoryRepositoryPort;
import com.community.soap.catalog.application.request.category.CreateCategoryRequest;
import com.community.soap.catalog.application.request.category.UpdateCategoryRequest;
import com.community.soap.catalog.application.response.category.CreateCategoryResponse;
import com.community.soap.catalog.application.response.category.GetCategoriesResponse;
import com.community.soap.catalog.application.response.category.UpdateCategoryResponse;
import com.community.soap.catalog.domain.entity.Category;
import com.community.soap.catalog.domain.exception.CategoryErrorCode;
import com.community.soap.catalog.domain.exception.CategoryException;
import com.community.soap.common.snowflake.Snowflake;
import com.community.soap.common.util.PageResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j(topic = "CategoryService")
@RequiredArgsConstructor
@Service
public class CategoryService implements CategoryUseCase {

    private final CategoryRepositoryPort categoryRepositoryPort;
    private final Snowflake snowflake;

    public void existsByName(String name) {
        if (categoryRepositoryPort.existsByName(name)) {
            throw new CategoryException(CategoryErrorCode.CATEGORY_NAME_DUPLICATED);
        }
    }

    public Category findByCategoryId(Long categoryId) {
        return categoryRepositoryPort.findById(categoryId)
                .orElseThrow(() -> new CategoryException(CategoryErrorCode.CATEGORY_NOT_FOUND));
    }

    @Transactional
    @Override
    public CreateCategoryResponse createCategory(Long userId, CreateCategoryRequest request) {
        existsByName(request.name());

        Category category = Category.of(
                snowflake.nextId(),
                request.name(),
                request.description(),
                userId
        );

        Category save = categoryRepositoryPort.save(category);

        return CreateCategoryResponse.from(save);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<GetCategoriesResponse> getCategories(
            String q,
            Pageable pageable,
            boolean all) {
        if (all) {
            // 전체 보기
            List<Category> list = (q == null || q.isBlank())
                    ? categoryRepositoryPort.findByIsDeletedFalseOrderByCreatedAtDesc()
                    : categoryRepositoryPort.findByIsDeletedFalseAndNameContainingIgnoreCaseOrderByCreatedAtDesc(
                            q);

            return new PageResponse<>(
                    list.stream().map(GetCategoriesResponse::from).toList(),
                    0, // 0부터 시작
                    list.size(),
                    list.size(),
                    1, // 카테고리 페이징 1까지만 설정
                    true
            );
        }
        Page<Category> page = (q == null || q.isBlank())
                ? categoryRepositoryPort.findByIsDeletedFalse(pageable)
                : categoryRepositoryPort.findByIsDeletedFalseAndNameContainingIgnoreCase(q,
                        pageable);

        Page<GetCategoriesResponse> mapped = page.map(GetCategoriesResponse::from);
        return PageResponse.from(mapped);

    }

    @Transactional
    @Override
    public UpdateCategoryResponse updateCategory(Long userId, Long categoryId,
            UpdateCategoryRequest request) {
        Category findCategory = findByCategoryId(categoryId);

        if (!request.newName().isBlank()) {
            findCategory.updateName(userId, request.newName());
        }

        if (!request.newDescription().isBlank()) {
            findCategory.updateDescription(userId, request.newDescription());
        }

        return UpdateCategoryResponse.from(findCategory);
    }

    @Transactional
    @Override
    public void softDeleteCategory(Long userId, Long categoryId) {
        Category byCategoryId = findByCategoryId(categoryId);

        byCategoryId.updateIsDeleted(userId, true);
    }
}

