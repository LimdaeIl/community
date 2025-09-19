package com.community.soap.payment.infrastructure;

public interface CatalogPort {
    void increaseStock(Long productId, String skuCode, int quantity);
    // (선택) decreaseStock(...)도 추후 필요 시 정의
}
