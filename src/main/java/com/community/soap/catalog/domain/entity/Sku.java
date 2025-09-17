package com.community.soap.catalog.domain.entity;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class Sku {

    // 가장 기본 값들만: 코드, 가격, 재고
    private String code;
    private Integer price;
    private Integer stock;

    public void increaseStock(int quantity) {
        this.stock = (this.stock == null ? 0 : this.stock) + quantity;
    }

    public void decreaseStock(int quantity) {
        int cur = (this.stock == null ? 0 : this.stock) - quantity;
        this.stock = Math.max(cur, 0);
    }

    @Builder(access = AccessLevel.PRIVATE)
    private Sku(String code, Integer price, Integer stock) {
        this.code = code;
        this.price = price;
        this.stock = stock;
    }

    public static Sku of(String code, Integer price, Integer stock) {
        return Sku.builder()
                .code(code)
                .price(price)
                .stock(stock)
                .build();
    }

    public void apply(String code, Integer price, Integer stock) {
        if (code != null)  this.code = code;
        if (price != null) this.price = price;
        if (stock != null) this.stock = stock;
    }
}
