package com.community.soap.catalog.application.service;

import com.community.soap.catalog.application.port.in.ProductUseCase;
import com.community.soap.catalog.application.port.out.ProductRepositoryPort;
import com.community.soap.catalog.application.request.product.CreateProductRequest;
import com.community.soap.catalog.application.request.product.DecreaseProductRequest;
import com.community.soap.catalog.application.request.product.IncreaseProductRequest;
import com.community.soap.catalog.application.request.product.ProductSearchCondition;
import com.community.soap.catalog.application.request.product.UpdateProductRequest;
import com.community.soap.catalog.application.response.product.CreateProductResponse;
import com.community.soap.catalog.application.response.product.GetProductResponse;
import com.community.soap.catalog.domain.entity.Product;
import com.community.soap.catalog.domain.entity.Sku;
import com.community.soap.catalog.domain.exception.ProductErrorCode;
import com.community.soap.catalog.domain.exception.ProductException;
import com.community.soap.common.snowflake.Snowflake;
import com.community.soap.common.util.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ProductService implements ProductUseCase {

    private final Snowflake snowflake;
    private final ProductRepositoryPort productRepositoryPort;

    private void existsBySkuCode(String skuCode) {
        if (productRepositoryPort.existsBySkuCode(skuCode)) {
            throw new ProductException(ProductErrorCode.SKU_CODE_DUPLICATED);
        }
    }

    private void existsByProductId(Long productId) {
        if (!productRepositoryPort.existsByProductId(productId)) {
            throw new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND);
        }
    }

    private Product findProductByProductId(Long productId) {
        return productRepositoryPort.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));
    }

    private void assertUpdatable(Product p) {
        if (Boolean.TRUE.equals(p.getIsDeleted())) {
            throw new ProductException(ProductErrorCode.PRODUCT_SOFT_DELETED);
        }
    }

    @Transactional
    @Override
    public CreateProductResponse createProduct(CreateProductRequest request) {
        existsBySkuCode(request.skuCode());

        Sku sku = Sku.of(
                request.skuCode(),
                request.price(),
                request.stock()
        );

        Product product = Product.of(
                snowflake.nextId(),
                request.name(),
                request.description(),
                request.categoryId(),
                request.productStatus(),
                sku,
                0L
        );

        Product saved = productRepositoryPort.save(product);

        return CreateProductResponse.from(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public GetProductResponse getProductDetail(Long productId) {
        existsByProductId(productId);
        Product productById = findProductByProductId(productId);

        return GetProductResponse.from(productById);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<GetProductResponse> getProducts(ProductSearchCondition c) {
        int page = (c.page() == null || c.page() < 0) ? 0 : c.page();
        int size = (c.size() == null || c.size() <= 0) ? 20 : Math.min(c.size(), 200);

        if (c.minPrice() != null && c.maxPrice() != null && c.maxPrice() < c.minPrice()) {
            throw new ProductException(ProductErrorCode.INVALID_PRICE_RANGE);
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<GetProductResponse> pageResult = productRepositoryPort
                .findAllByCondition(c, pageable)
                .map(GetProductResponse::from);

        return PageResponse.from(pageResult);
    }

    @Transactional
    @Override
    public GetProductResponse increaseProduct(Long productId, IncreaseProductRequest request) {
        Product product = findProductByProductId(productId);
        assertUpdatable(product);
        product.getSku().increaseStock(request.quantity());

        return GetProductResponse.from(product);
    }


    @Transactional
    @Override
    public GetProductResponse decreaseProduct(Long productId, DecreaseProductRequest request) {
        Product product = findProductByProductId(productId);
        assertUpdatable(product);
        product.getSku().decreaseStock(request.quantity());

        return GetProductResponse.from(product);
    }

    @Transactional
    @Override
    public GetProductResponse updateProduct(Long productId, UpdateProductRequest request) {
        Product product = findProductByProductId(productId);
        assertUpdatable(product); // 삭제/INACTIVE 등 업데이트 불가 가드

        // (선택) 문자열 유효성 – 비어있으면 무시/에러 정책은 팀 규칙대로
        if (request.name() != null && !request.name().isBlank()) {
            product.changeName(request.name().trim());
        }
        if (request.description() != null && !request.description().isBlank()) {
            product.changeDescription(request.description().trim());
        }

        if (request.categoryId() != null) {
            product.changeCategory(request.categoryId());
        }

        if (request.productStatus() != null) {
            product.changeStatus(request.productStatus());
        }

        // SKU 코드가 바뀌는 경우에만 중복 체크
        if (request.skuCode() != null) {
            String current = product.getSku().getCode();
            if (!request.skuCode().equals(current)) {
                if (productRepositoryPort.existsBySkuCode(request.skuCode())) {
                    throw new ProductException(ProductErrorCode.SKU_CODE_DUPLICATED);
                }
            }
        }

        // 가격/재고/코드 중 들어온 값만 반영
        if (request.skuCode() != null || request.price() != null || request.stock() != null) {
            product.changeSku(request.skuCode(), request.price(), request.stock());
        }

        // 감사 필드
        product.updatedBy(0L); // 실제 사용자 ID로 교체 (예: SecurityContext에서 꺼내기)

        // 더티체킹으로 UPDATE 반영
        return GetProductResponse.from(product);
    }

    @Transactional
    @Override
    public void softDeleteProduct(Long productId, UpdateProductRequest request) {
        Product productById = findProductByProductId(productId);

        productById.softDelete();
        productById.updatedBy(0L);
    }
}
