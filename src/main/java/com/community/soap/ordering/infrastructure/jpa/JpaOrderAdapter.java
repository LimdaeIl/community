package com.community.soap.ordering.infrastructure.jpa;

import com.community.soap.ordering.application.port.out.OrderRepositoryPort;
import com.community.soap.ordering.application.request.OrderSearchCondition;
import com.community.soap.ordering.domain.entity.Order;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaOrderAdapter extends JpaRepository<Order, Long>, OrderRepositoryPort {


    @Query(
        value =
            "SELECT o.* " +
            "FROM c_orders o " +
            "WHERE 1=1 " +
            // 상태
            "  AND (:#{#cond.status?.name()} IS NULL OR o.order_status = :#{#cond.status?.name()}) " +
            // 주문자
            "  AND (:#{#cond.createdBy} IS NULL OR o.created_by = :#{#cond.createdBy}) " +
            // 기간
            "  AND (:#{#cond.from} IS NULL OR o.created_at >= :#{#cond.from}) " +
            "  AND (:#{#cond.to}   IS NULL OR o.created_at <  :#{#cond.to}) " +
            // 합계 범위
            "  AND (:#{#cond.minTotal} IS NULL OR o.total_amount >= :#{#cond.minTotal}) " +
            "  AND (:#{#cond.maxTotal} IS NULL OR o.total_amount <= :#{#cond.maxTotal}) " +
            // 키워드: 아이템(상품명/sku)에서 존재 여부로 필터
            "  AND ( :#{#cond.keyword} IS NULL OR :#{#cond.keyword} = '' OR EXISTS ( " +
            "        SELECT 1 " +
            "        FROM c_order_item oi " +
            "        WHERE oi.order_id = o.order_id " +
            "          AND (LOWER(oi.product_name) LIKE CONCAT('%', LOWER(:#{#cond.keyword}), '%') " +
            "               OR LOWER(oi.sku_code)    LIKE CONCAT('%', LOWER(:#{#cond.keyword}), '%')) " +
            "      ) ) " +
            // 동적 정렬 (인젝션 방지)
            "ORDER BY " +
            "  CASE WHEN :sort = 'created_at'  AND :dir = 'asc'  THEN o.created_at   END ASC, " +
            "  CASE WHEN :sort = 'created_at'  AND :dir = 'desc' THEN o.created_at   END DESC, " +
            "  CASE WHEN :sort = 'total_amount' AND :dir = 'asc'  THEN o.total_amount END ASC, " +
            "  CASE WHEN :sort = 'total_amount' AND :dir = 'desc' THEN o.total_amount END DESC, " +
            "  o.order_id DESC " +    // 안정 정렬
            "LIMIT :limit OFFSET :offset",
        nativeQuery = true
    )
    List<Order> findAllByConditionRaw(
        @Param("cond") OrderSearchCondition cond,
        @Param("sort") String sort,
        @Param("dir")  String dir,
        @Param("limit") int limit,
        @Param("offset") int offset
    );

    @Query(
        value =
            "SELECT COUNT(*) " +
            "FROM c_orders o " +
            "WHERE 1=1 " +
            "  AND (:#{#cond.status?.name()} IS NULL OR o.order_status = :#{#cond.status?.name()}) " +
            "  AND (:#{#cond.createdBy} IS NULL OR o.created_by = :#{#cond.createdBy}) " +
            "  AND (:#{#cond.from} IS NULL OR o.created_at >= :#{#cond.from}) " +
            "  AND (:#{#cond.to}   IS NULL OR o.created_at <  :#{#cond.to}) " +
            "  AND (:#{#cond.minTotal} IS NULL OR o.total_amount >= :#{#cond.minTotal}) " +
            "  AND (:#{#cond.maxTotal} IS NULL OR o.total_amount <= :#{#cond.maxTotal}) " +
            "  AND ( :#{#cond.keyword} IS NULL OR :#{#cond.keyword} = '' OR EXISTS ( " +
            "        SELECT 1 " +
            "        FROM c_order_item oi " +
            "        WHERE oi.order_id = o.order_id " +
            "          AND (LOWER(oi.product_name) LIKE CONCAT('%', LOWER(:#{#cond.keyword}), '%') " +
            "               OR LOWER(oi.sku_code)    LIKE CONCAT('%', LOWER(:#{#cond.keyword}), '%')) " +
            "      ) ) ",
        nativeQuery = true
    )
    long countAllByCondition(@Param("cond") OrderSearchCondition cond);


    default Page<Order> findAllByCondition(OrderSearchCondition cond, Pageable pageable) {
        int page = Math.max(0, pageable.getPageNumber());
        int size = pageable.getPageSize() <= 0 ? 20 : Math.min(pageable.getPageSize(), 200);
        int limit = size;
        int offset = page * size;

        String sortToken = cond.normalizedSort(); // created_at || total_amount
        String dir = cond.normalizedOrder(); // asc || desc

        List<Order> rows = findAllByConditionRaw(cond, sortToken, dir, limit, offset);
        long total = countAllByCondition(cond);

        return new PageImpl<>(rows, PageRequest.of(page, size), total);
    }

    @Query("""
        select o
        from Order o
        left join fetch o.orderItems oi
        where o.orderId = :orderId
    """)
    Optional<Order> findByIdWithItems(@Param("orderId") Long orderId);
}
