package com.community.soap.catalog.infrastructure.jpa;

import com.community.soap.catalog.application.port.out.ProductRepositoryPort;
import com.community.soap.catalog.application.request.product.ProductSearchCondition;
import com.community.soap.catalog.domain.entity.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaProductAdapter extends JpaRepository<Product, Long>, ProductRepositoryPort {

    // SKU 중복(소프트삭제 제외)
    @Query("select count(p) > 0 from Product p where p.sku.code = :code and p.isDeleted = false")
    boolean existsBySkuCode(@Param("code") String code);

    // === existsByProductId (소프트삭제 제외) ===
    // 파생 쿼리로 간단/빠름
    boolean existsByProductIdAndIsDeletedFalse(Long productId);

    @Override
    default boolean existsByProductId(Long productId) {
        return existsByProductIdAndIsDeletedFalse(productId);
    }

    @Override
    Optional<Product> findById(Long productId);

    @Query(
        value =
            "SELECT p.* " +
            "FROM s_product p " +
            "WHERE p.is_deleted = false " +
            "  AND p.product_status <> 'INACTIVE' " +
            "  AND (:#{#cond.categoryId} IS NULL OR p.category_id = :#{#cond.categoryId}) " +
            "  AND (:#{#cond.minPrice} IS NULL OR p.sku_price >= :#{#cond.minPrice}) " +   // <<< 변경
            "  AND (:#{#cond.maxPrice} IS NULL OR p.sku_price <= :#{#cond.maxPrice}) " +   // <<< 변경
            "  AND (:#{#cond.keyword} IS NULL OR :#{#cond.keyword} = '' " +
            "       OR LOWER(p.name) LIKE CONCAT('%', LOWER(:#{#cond.keyword}), '%')) " +
            "ORDER BY " +
            "  CASE WHEN :sort = 'created_at' AND :dir = 'asc'  THEN p.created_at END ASC, " +
            "  CASE WHEN :sort = 'created_at' AND :dir = 'desc' THEN p.created_at END DESC, " +
            "  CASE WHEN :sort = 'name'       AND :dir = 'asc'  THEN p.name       END ASC, " +
            "  CASE WHEN :sort = 'name'       AND :dir = 'desc' THEN p.name       END DESC, " +
            "  CASE WHEN :sort = 'sku_price'  AND :dir = 'asc'  THEN p.sku_price  END ASC, " + // <<< 변경
            "  CASE WHEN :sort = 'sku_price'  AND :dir = 'desc' THEN p.sku_price  END DESC, " + // <<< 변경
            "  p.product_id DESC " +
            "LIMIT :limit OFFSET :offset",
        nativeQuery = true
    )
    List<Product> findAllByConditionRaw(
        @Param("cond")  ProductSearchCondition condition,
        @Param("sort")  String sort,   // created_at | name | sku_price
        @Param("dir")   String dir,    // asc | desc
        @Param("limit") int limit,
        @Param("offset") int offset
    );

    @Query(
        value =
            "SELECT COUNT(*) " +
            "FROM s_product p " +
            "WHERE p.is_deleted = false " +
            "  AND p.product_status <> 'INACTIVE' " +
            "  AND (:#{#cond.categoryId} IS NULL OR p.category_id = :#{#cond.categoryId}) " +
            "  AND (:#{#cond.minPrice} IS NULL OR p.sku_price >= :#{#cond.minPrice}) " + // <<< 변경
            "  AND (:#{#cond.maxPrice} IS NULL OR p.sku_price <= :#{#cond.maxPrice}) " + // <<< 변경
            "  AND (:#{#cond.keyword} IS NULL OR :#{#cond.keyword} = '' " +
            "       OR LOWER(p.name) LIKE CONCAT('%', LOWER(:#{#cond.keyword}), '%'))",
        nativeQuery = true
    )
    long countAllByCondition(@Param("cond") ProductSearchCondition condition);

    @Override
    default Page<Product> findAllByCondition(ProductSearchCondition condition, Pageable pageable) {
        int page  = Math.max(0, pageable.getPageNumber());
        int size  = pageable.getPageSize() <= 0 ? 20 : Math.min(pageable.getPageSize(), 200);
        int limit = size;
        int offset = page * size;

        // 정렬 키 매핑: price -> sku_price
        String sortToken = switch (condition.normalizedSort()) {
            case "name" -> "name";
            case "price" -> "sku_price";      // <<< 변경 포인트
            case "createdAt" -> "created_at";
            default -> "created_at";
        };
        String dir = condition.normalizedOrder(); // asc|desc

        List<Product> rows = findAllByConditionRaw(condition, sortToken, dir, limit, offset);
        long total = countAllByCondition(condition);

        return new PageImpl<>(rows, PageRequest.of(page, size), total);
    }
}
