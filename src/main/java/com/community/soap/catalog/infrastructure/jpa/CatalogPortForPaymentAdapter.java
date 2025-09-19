package com.community.soap.catalog.infrastructure.jpa;

import com.community.soap.catalog.application.port.out.ProductRepositoryPort;
import com.community.soap.catalog.domain.entity.Product;
import com.community.soap.payment.domain.exception.PaymentErrorCode;
import com.community.soap.payment.domain.exception.PaymentException;
import com.community.soap.payment.infrastructure.CatalogPort; // ★결제 포트
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component("catalogPortForPayment") // ★ 명시적인 빈 이름
@RequiredArgsConstructor
@Transactional
public class CatalogPortForPaymentAdapter implements CatalogPort {

    private final ProductRepositoryPort productRepositoryPort;

    @Override
    public void increaseStock(Long productId, String skuCode, int quantity) {
        Product p = productRepositoryPort.findById(productId)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.ORDER_NOT_FOUND));

        Integer before = p.getSku().getStock();
        p.getSku().increaseStock(quantity);
        p.updatedBy(0L);
        // 임시 로그
         log.info("productId={} stock {} -> {}", productId, before, p.getSku().getStock());

        productRepositoryPort.save(p);
    }
}
