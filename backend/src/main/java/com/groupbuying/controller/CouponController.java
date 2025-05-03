package com.groupbuying.controller;

import com.groupbuying.dto.CouponDTO;
import com.groupbuying.service.CouponService;
import com.groupbuying.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {
    private final CouponService couponService;

    @PostMapping("/draw")
    public ResponseEntity<CouponDTO> drawCoupon() {
        Long userId = SecurityUtil.getCurrentUser().getId();
        return ResponseEntity.ok(couponService.drawCoupon(userId));
    }

    @GetMapping("/my")
    public ResponseEntity<List<CouponDTO>> getMyCoupons() {
        Long userId = SecurityUtil.getCurrentUser().getId();
        return ResponseEntity.ok(couponService.getUserCoupons(userId));
    }

    @GetMapping("/can-draw")
    public ResponseEntity<Boolean> canDrawToday() {
        Long userId = SecurityUtil.getCurrentUser().getId();
        return ResponseEntity.ok(couponService.canDrawToday(userId));
    }

    @PostMapping("/{id}/use")
    public ResponseEntity<CouponDTO> useCoupon(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUser().getId();
        return ResponseEntity.ok(couponService.useCoupon(id, userId));
    }
} 