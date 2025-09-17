package com.community.soap.catalog.application.port.out;

import com.community.soap.catalog.domain.entity.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryRepositoryPort {

    boolean existsByName(String name);

    Page<Category> findByIsDeletedFalse(Pageable pageable);

    Page<Category> findByIsDeletedFalseAndNameContainingIgnoreCase(String q, Pageable pageable);

    // 전체 조회(ALL 용)
    List<Category> findByIsDeletedFalseOrderByCreatedAtDesc();

    List<Category> findByIsDeletedFalseAndNameContainingIgnoreCaseOrderByCreatedAtDesc(String q);

    Category save(Category category);

    Optional<Category> findById(Long categoryId);
}
