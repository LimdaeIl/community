package com.community.soap.catalog.application.port.in;

import com.community.soap.catalog.application.request.product.CreateProductRequest;
import com.community.soap.catalog.application.request.product.DecreaseProductRequest;
import com.community.soap.catalog.application.request.product.IncreaseProductRequest;
import com.community.soap.catalog.application.request.product.ProductSearchCondition;
import com.community.soap.catalog.application.request.product.UpdateProductRequest;
import com.community.soap.catalog.application.response.product.CreateProductResponse;
import com.community.soap.catalog.application.response.product.GetProductResponse;
import com.community.soap.catalog.application.response.product.IncreaseProductResponse;
import com.community.soap.common.util.PageResponse;

public interface ProductUseCase {

    CreateProductResponse createProduct(CreateProductRequest request);

    GetProductResponse getProductDetail(Long productId);

    PageResponse<GetProductResponse> getProducts(ProductSearchCondition condition);

    GetProductResponse increaseProduct(Long productId, IncreaseProductRequest request);

    GetProductResponse decreaseProduct(Long productId, DecreaseProductRequest request);

    GetProductResponse updateProduct(Long productId, UpdateProductRequest request);

    void softDeleteProduct(Long productId, UpdateProductRequest request);
}
