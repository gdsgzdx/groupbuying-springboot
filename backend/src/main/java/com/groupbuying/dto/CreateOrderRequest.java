package com.groupbuying.dto;

import lombok.Data;

@Data
public class CreateOrderRequest {
    private Long productId;
    private String paymentPassword;
    private Long couponId;
} 