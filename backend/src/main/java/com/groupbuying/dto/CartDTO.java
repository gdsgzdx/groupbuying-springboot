package com.groupbuying.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CartDTO {
    private Long id;
    private Long userId;
    private Long productId;
    private String productName;
    private BigDecimal price;
    private String imageUrl;
    private Integer quantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 