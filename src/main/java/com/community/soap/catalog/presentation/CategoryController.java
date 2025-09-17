package com.community.soap.catalog.presentation;

import com.community.soap.catalog.application.port.in.CategoryUseCase;
import com.community.soap.catalog.application.request.category.CreateCategoryRequest;
import com.community.soap.catalog.application.request.category.UpdateCategoryRequest;
import com.community.soap.catalog.application.response.category.CreateCategoryResponse;
import com.community.soap.catalog.application.response.category.GetCategoriesResponse;
import com.community.soap.catalog.application.response.category.UpdateCategoryResponse;
import com.community.soap.common.aop.Permission;
import com.community.soap.common.resolver.CurrentUser;
import com.community.soap.common.resolver.CurrentUserInfo;
import com.community.soap.common.util.PageResponse;
import com.community.soap.user.domain.entity.UserRole;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RequiredArgsConstructor
@RequestMapping("/api/v1/categories")
@RestController
public class CategoryController {

    private final CategoryUseCase categoryUseCase;

    @Permission(value = {UserRole.ADMIN, UserRole.MANAGER})
    @PostMapping
    public ResponseEntity<CreateCategoryResponse> createCategory(
            @CurrentUser CurrentUserInfo info,
            @RequestBody @Valid CreateCategoryRequest request
    ) {
        CreateCategoryResponse response = categoryUseCase.createCategory(info.userId(), request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // GET /api/v1/categories?page=0&size=8&sort=createdAt,desc
    // GET /api/v1/categories?all=true
    // GET /api/v1/categories?q=soap&page=0&size=8
    @GetMapping
    public ResponseEntity<PageResponse<GetCategoriesResponse>> getCategories(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "false") boolean all,
            @ParameterObject @PageableDefault(size = 8, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC)
            Pageable pageable
    ) {
        PageResponse<GetCategoriesResponse> res = categoryUseCase.getCategories(q, pageable, all);
        return ResponseEntity.ok(res);
    }

    @Permission(value = {UserRole.ADMIN, UserRole.MANAGER})
    @PatchMapping("/{categoryId}")
    public ResponseEntity<UpdateCategoryResponse> updateCategory(
            @CurrentUser CurrentUserInfo info,
            @PathVariable Long categoryId,
            @RequestBody UpdateCategoryRequest request
    ) {
        UpdateCategoryResponse response = categoryUseCase.updateCategory(info.userId(), categoryId, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @Permission(value = {UserRole.ADMIN, UserRole.MANAGER})
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> softDeleteCategory(
            @CurrentUser CurrentUserInfo info,
            @PathVariable Long categoryId
    ) {
        categoryUseCase.softDeleteCategory(info.userId(), categoryId);

        return ResponseEntity
                .noContent()
                .build();
    }
}
