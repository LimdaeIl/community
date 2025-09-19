package com.community.soap.payment.infrastructure;

public record OrderLineItem(Long productId, String skuCode, int quantity) {}
