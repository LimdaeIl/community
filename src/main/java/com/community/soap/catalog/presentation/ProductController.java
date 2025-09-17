package com.community.soap.catalog.presentation;

import com.community.soap.catalog.application.port.in.ProductUseCase;
import com.community.soap.catalog.application.request.product.CreateProductRequest;
import com.community.soap.catalog.application.request.product.DecreaseProductRequest;
import com.community.soap.catalog.application.request.product.IncreaseProductRequest;
import com.community.soap.catalog.application.request.product.ProductSearchCondition;
import com.community.soap.catalog.application.request.product.UpdateProductRequest;
import com.community.soap.catalog.application.response.product.CreateProductResponse;
import com.community.soap.catalog.application.response.product.GetProductResponse;
import com.community.soap.common.util.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
@RestController
public class ProductController {

    private final ProductUseCase productUseCase;


    @PostMapping
    public ResponseEntity<CreateProductResponse> createProduct(
            @RequestBody CreateProductRequest request
    ) {
        CreateProductResponse response = productUseCase.createProduct(request);

        return ResponseEntity.
                status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<GetProductResponse> getProductDetail(
            @PathVariable Long productId
    ) {
        GetProductResponse response = productUseCase.getProductDetail(productId);

        return ResponseEntity.
                status(HttpStatus.OK)
                .body(response);
    }


    @GetMapping
    public ResponseEntity<PageResponse<GetProductResponse>> getProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String order,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        ProductSearchCondition condition = new ProductSearchCondition(
                categoryId, minPrice, maxPrice, keyword, sort, order, page, size);
        PageResponse<GetProductResponse> response = productUseCase.getProducts(condition);
        return ResponseEntity.ok(response);
    }


    @PatchMapping("/{productId}/increase")
    public ResponseEntity<GetProductResponse> increaseProduct(
            @PathVariable Long productId,
            @RequestBody @Valid IncreaseProductRequest request
    ) {
        GetProductResponse response = productUseCase.increaseProduct(productId, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PatchMapping("/{productId}/decrease")
    public ResponseEntity<GetProductResponse> decreaseProduct(
            @PathVariable Long productId,
            @RequestBody DecreaseProductRequest request
    ) {
        GetProductResponse response = productUseCase.decreaseProduct(productId, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PatchMapping("/{productId}/update")
    public ResponseEntity<GetProductResponse> updateProduct(
            @PathVariable Long productId,
            @RequestBody UpdateProductRequest request
    ) {
        GetProductResponse response = productUseCase.updateProduct(productId, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @DeleteMapping("/{productId}/delete")
    public ResponseEntity<GetProductResponse> softDeleteProduct(
            @PathVariable Long productId,
            @RequestBody @Valid UpdateProductRequest request
    ) {
        productUseCase.softDeleteProduct(productId, request);

        return ResponseEntity
                .noContent()
                .build();
    }


}
