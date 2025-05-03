package com.groupbuying.repository;

import com.groupbuying.entity.Coupon;
import com.groupbuying.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    List<Coupon> findByUserAndUsedFalseAndExpiryTimeAfter(User user, LocalDateTime now);
    
    @Query("SELECT COUNT(c) FROM Coupon c WHERE c.user = :user AND c.createTime >= :startOfDay")
    int countTodayCoupons(@Param("user") User user, @Param("startOfDay") LocalDateTime startOfDay);
} 