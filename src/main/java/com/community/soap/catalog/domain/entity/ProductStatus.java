package com.community.soap.catalog.domain.entity;

import lombok.Getter;


@Getter
public enum ProductStatus {
    ACTIVE,     // 판매중
    INACTIVE,   // 비활성 (진열 X)
    OUT_OF_STOCK // 품절
}
