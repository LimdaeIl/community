package com.community.soap.catalog.domain.entity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "s_product")
@Entity
public class Product {

    @Id
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "category_id")
    private Long categoryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_status", nullable = false, length = 40)
    private ProductStatus productStatus;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "code", column = @Column(name = "sku_code", nullable = false)),
            @AttributeOverride(name = "price", column = @Column(name = "sku_price", nullable = false)),
            @AttributeOverride(name = "stock", column = @Column(name = "sku_stock", nullable = false))
    })
    private Sku sku;

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

    private Product(
            Long productId,
            String name,
            String description,
            Long categoryId,
            ProductStatus productStatus,
            Sku sku,
            Long createdBy) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
        this.productStatus = productStatus;
        this.sku = sku;
        this.isDeleted = Boolean.FALSE;
        this.createdAt = LocalDateTime.now();
        this.createdBy = createdBy;
        this.updatedAt = null;
        this.updatedBy = null;
    }

    public static Product of(
            Long productId,
            String name,
            String description,
            Long categoryId,
            ProductStatus productStatus,
            Sku sku,
            Long createdBy) {
        return new Product(
                productId,
                name,
                description,
                categoryId,
                productStatus,
                sku,
                createdBy);
    }

    public void updatedBy(Long userId) {
        this.updatedBy = userId;
        this.updatedAt = LocalDateTime.now();
    }

    public void changeName(String name) {
        this.name = name;
    }

    public void changeDescription(String description) {
        this.description = description;
    }

    public void changeCategory(Long categoryId) {
        this.categoryId = categoryId;
    }

    public void changeStatus(ProductStatus status) {
        this.productStatus = status;
    }

    public void changeSku(String code, Integer price, Integer stock) {
        this.sku.apply(code, price, stock);
    }

    public void softDelete() {
        this.isDeleted = Boolean.TRUE;
    }
}
