package com.community.soap.ordering.application.port.out;

public interface CatalogPort {
    ProductSnapshot getProductSnapshot(Long productId);
    boolean tryReserveStock(Long productId, int quantity); // V1: 비원자적 구현
}
