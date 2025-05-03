package com.groupbuying.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Entity
@Table(name = "group_buys")
public class GroupBuy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 8)
    private String inviteCode;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "leader_id", nullable = false)
    private User leader;

    @Column(name = "required_members", nullable = false)
    private Integer requiredSize;

    @Column(name = "current_members", nullable = false)
    private Integer currentSize = 1;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status = Status.IN_PROGRESS;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime expiryTime;

    @ManyToMany
    @JoinTable(
        name = "group_buy_members",
        joinColumns = @JoinColumn(name = "group_buy_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> participants = new HashSet<>();

    @Column(nullable = false)
    private BigDecimal groupPrice;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum Status {
        IN_PROGRESS,
        SUCCESS,
        FAILED
    }

    @PrePersist
    public void prePersist() {
        if (expiryTime == null) {
            // 设置结束时间为创建时间后4小时
            expiryTime = LocalDateTime.now().plusHours(4);
        }
        if (groupPrice == null && product != null) {
            // 设置拼团价格为原价的8折
            groupPrice = product.getPrice().multiply(new BigDecimal("0.8"));
        }
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryTime);
    }

    public boolean isFull() {
        return currentSize >= requiredSize;
    }

    public boolean canJoin() {
        return "IN_PROGRESS".equals(status) && !isExpired() && !isFull();
    }

    public Set<User> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<User> participants) {
        this.participants = participants;
    }

    public Integer getCurrentSize() {
        return currentSize;
    }

    public void setCurrentSize(Integer currentSize) {
        this.currentSize = currentSize;
    }

    public Integer getRequiredSize() {
        return requiredSize;
    }

    public void setRequiredSize(Integer requiredSize) {
        this.requiredSize = requiredSize;
    }

    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(LocalDateTime expiryTime) {
        this.expiryTime = expiryTime;
    }
} 