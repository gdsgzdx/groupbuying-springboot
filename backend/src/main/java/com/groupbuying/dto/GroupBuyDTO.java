package com.groupbuying.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class GroupBuyDTO {
    private Long id;
    private String inviteCode;
    private Long productId;
    private String productName;
    private String productImage;
    private BigDecimal originalPrice;
    private BigDecimal groupPrice;
    private Long leaderId;
    private String leaderName;
    private String leaderAvatar;
    private Integer requiredSize;
    private Integer currentSize;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime expiryTime;
    private Set<UserDTO> participants;
    
    // 用于前端显示
    private Long remainingSeconds;
} 