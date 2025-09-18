package com.community.soap.ordering.infrastructure.adapter;

import com.community.soap.catalog.application.port.out.ProductRepositoryPort;
import com.community.soap.catalog.domain.entity.Product;
import com.community.soap.ordering.application.port.out.CatalogPort;
import com.community.soap.ordering.application.port.out.ProductSnapshot;
import com.community.soap.ordering.domain.exception.OrderErrorCode;
import com.community.soap.ordering.domain.exception.OrderException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CatalogPortAdapter implements CatalogPort {

    private final ProductRepositoryPort productRepositoryPort;

    @Override
    public ProductSnapshot getProductSnapshot(Long productId) {
        Product p = productRepositoryPort.findById(productId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.PRODUCT_NOT_FOUND));

        return new ProductSnapshot(
                p.getProductId(),
                p.getName(),
                p.getSku().getCode(),
                p.getSku().getPrice(),
                p.getProductStatus(),
                Boolean.TRUE.equals(p.getIsDeleted())
        );
    }

    @Override
    public boolean tryReserveStock(Long productId, int quantity) {
        // V1: 비원자적 처리 (동시성 고려 X)
        Product p = productRepositoryPort.findById(productId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.PRODUCT_NOT_FOUND));

        int cur = p.getSku().getStock() == null ? 0 : p.getSku().getStock();
        if (quantity <= 0) {
            return false;
        }
        if (cur < quantity) {
            return false; // 재고 부족
        }

        // 충분하면 감소
        p.getSku().decreaseStock(quantity);            // 감소 로직 내부는 0 바닥처리지만, 위에서 충분성 검증했음
        p.updatedBy(0L);                               // 감사필드(실사용자 ID로 교체)
        return true;                                   // 더티체킹으로 커밋 시 UPDATE 발생
    }
}