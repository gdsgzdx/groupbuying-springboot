package com.groupbuying.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private String address;
    private BigDecimal accountBalance;
    private boolean active;
    private String avatar;
} 