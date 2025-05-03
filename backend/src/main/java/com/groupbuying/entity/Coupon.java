package com.groupbuying.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "coupons")
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "discount_rate")
    private Double discountRate; // 折扣率：0.95, 0.9, 0.85, 0.8, 0.75

    private LocalDateTime createTime;

    private LocalDateTime expiryTime;

    @Column(name = "used")
    private Boolean used = false;

    @PrePersist
    public void prePersist() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
        if (expiryTime == null) {
            expiryTime = createTime.plusDays(7);
        }
    }
} 