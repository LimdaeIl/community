package com.community.soap.catalog.application.port.out;

import com.community.soap.catalog.application.request.product.ProductSearchCondition;
import com.community.soap.catalog.domain.entity.Product;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepositoryPort {
    Product save(Product product);
    boolean existsBySkuCode(String code);
    Optional<Product> findById(Long productId);

    boolean existsByProductId(Long productId);
    Page<Product> findAllByCondition(ProductSearchCondition condition, Pageable pageable);
}
