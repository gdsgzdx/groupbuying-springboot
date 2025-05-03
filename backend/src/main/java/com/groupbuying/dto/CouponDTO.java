package com.groupbuying.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CouponDTO {
    private Long id;
    private Long userId;
    private Double discount;
    private LocalDateTime createTime;
    private LocalDateTime expiryTime;
    private Boolean used;
    private String discountText; // 用于前端显示，如"95折"
} 