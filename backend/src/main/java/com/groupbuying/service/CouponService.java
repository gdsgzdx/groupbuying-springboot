package com.groupbuying.service;

import com.groupbuying.dto.CouponDTO;
import java.util.List;

public interface CouponService {
    CouponDTO drawCoupon(Long userId);
    List<CouponDTO> getUserCoupons(Long userId);
    CouponDTO useCoupon(Long couponId, Long userId);
    boolean canDrawToday(Long userId);
    CouponDTO getCoupon(Long couponId, Long userId);
} 