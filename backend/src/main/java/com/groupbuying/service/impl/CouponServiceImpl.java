package com.groupbuying.service.impl;

import com.groupbuying.dto.CouponDTO;
import com.groupbuying.entity.Coupon;
import com.groupbuying.entity.User;
import com.groupbuying.exception.BusinessException;
import com.groupbuying.exception.ErrorCode;
import com.groupbuying.repository.CouponRepository;
import com.groupbuying.repository.UserRepository;
import com.groupbuying.service.CouponService;
import com.groupbuying.util.DTOConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final DTOConverter dtoConverter;
    private final Random random = new Random();

    private static final double[] DISCOUNT_RATES = {0.95, 0.90, 0.85, 0.80, 0.75};

    @Override
    @Transactional
    public CouponDTO drawCoupon(Long userId) {
        log.info("用户[{}]开始抽取优惠券", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("用户[{}]不存在", userId);
                    return new BusinessException(ErrorCode.USER_NOT_FOUND);
                });

        if (!canDrawToday(userId)) {
            log.warn("用户[{}]今日已抽取过优惠券", userId);
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "今日已抽取过优惠券");
        }

        try {
            Coupon coupon = new Coupon();
            coupon.setUser(user);
            double discountRate = DISCOUNT_RATES[random.nextInt(DISCOUNT_RATES.length)];
            coupon.setDiscountRate(discountRate);
            coupon.setCreateTime(LocalDateTime.now());
            coupon.setExpiryTime(LocalDateTime.now().plusDays(7)); // 设置7天有效期
            coupon = couponRepository.save(coupon);
            
            log.info("用户[{}]成功抽取{}折优惠券，有效期至{}", userId, discountRate * 100, coupon.getExpiryTime());
            return dtoConverter.convertToCouponDTO(coupon);
        } catch (Exception e) {
            log.error("用户[{}]抽取优惠券时发生错误: {}", userId, e.getMessage());
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "抽取优惠券失败，请稍后重试");
        }
    }

    @Override
    public List<CouponDTO> getUserCoupons(Long userId) {
        log.info("获取用户[{}]的优惠券列表", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("用户[{}]不存在", userId);
                    return new BusinessException(ErrorCode.USER_NOT_FOUND);
                });

        try {
            List<Coupon> coupons = couponRepository.findByUserAndUsedFalseAndExpiryTimeAfter(
                    user, LocalDateTime.now());
            log.info("用户[{}]当前有{}张可用优惠券", userId, coupons.size());
            return coupons.stream()
                    .map(dtoConverter::convertToCouponDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取用户[{}]优惠券列表时发生错误: {}", userId, e.getMessage());
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "获取优惠券列表失败");
        }
    }

    @Override
    @Transactional
    public CouponDTO useCoupon(Long couponId, Long userId) {
        log.info("用户[{}]尝试使用优惠券[{}]", userId, couponId);
        
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> {
                    log.error("优惠券[{}]不存在", couponId);
                    return new BusinessException(ErrorCode.BUSINESS_ERROR, "优惠券不存在");
                });

        if (!coupon.getUser().getId().equals(userId)) {
            log.warn("用户[{}]尝试使用不属于自己的优惠券[{}]", userId, couponId);
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "无权使用此优惠券");
        }

        if (coupon.getUsed()) {
            log.warn("用户[{}]尝试使用已使用的优惠券[{}]", userId, couponId);
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "优惠券已使用");
        }

        if (coupon.getExpiryTime().isBefore(LocalDateTime.now())) {
            log.warn("用户[{}]尝试使用已过期的优惠券[{}]", userId, couponId);
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "优惠券已过期");
        }

        try {
            coupon.setUsed(true);
            coupon = couponRepository.save(coupon);
            log.info("用户[{}]成功使用优惠券[{}]", userId, couponId);
            return dtoConverter.convertToCouponDTO(coupon);
        } catch (Exception e) {
            log.error("用户[{}]使用优惠券[{}]时发生错误: {}", userId, couponId, e.getMessage());
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "使用优惠券失败");
        }
    }

    @Override
    public boolean canDrawToday(Long userId) {
        log.debug("检查用户[{}]今日是否可以抽取优惠券", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("用户[{}]不存在", userId);
                    return new BusinessException(ErrorCode.USER_NOT_FOUND);
                });

        try {
            LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
            int todayCoupons = couponRepository.countTodayCoupons(user, startOfDay);
            log.debug("用户[{}]今日已抽取{}张优惠券", userId, todayCoupons);
            return todayCoupons == 0;
        } catch (Exception e) {
            log.error("检查用户[{}]抽奖资格时发生错误: {}", userId, e.getMessage());
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "检查抽奖资格失败");
        }
    }

    @Override
    public CouponDTO getCoupon(Long couponId, Long userId) {
        log.info("获取优惠券信息: couponId={}, userId={}", couponId, userId);
        
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> {
                    log.error("优惠券[{}]不存在", couponId);
                    return new BusinessException(ErrorCode.BUSINESS_ERROR, "优惠券不存在");
                });

        if (!coupon.getUser().getId().equals(userId)) {
            log.warn("用户[{}]尝试获取不属于自己的优惠券[{}]", userId, couponId);
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "无权使用此优惠券");
        }

        if (coupon.getUsed()) {
            log.warn("用户[{}]尝试获取已使用的优惠券[{}]", userId, couponId);
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "优惠券已使用");
        }

        if (coupon.getExpiryTime().isBefore(LocalDateTime.now())) {
            log.warn("用户[{}]尝试获取已过期的优惠券[{}]", userId, couponId);
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "优惠券已过期");
        }

        return dtoConverter.convertToCouponDTO(coupon);
    }
} 