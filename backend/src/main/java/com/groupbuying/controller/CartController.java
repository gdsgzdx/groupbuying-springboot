package com.groupbuying.controller;

import com.groupbuying.common.Result;
import com.groupbuying.dto.CartDTO;
import com.groupbuying.service.CartService;
import com.groupbuying.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping
    public Result<CartDTO> addToCart(
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            log.info("用户 {} 添加商品 {} 到购物车，数量 {}", userId, productId, quantity);
            CartDTO result = cartService.addToCart(userId, productId, quantity);
            log.info("添加商品到购物车成功: {}", result);
            return Result.success(result);
        } catch (Exception e) {
            log.error("添加商品到购物车失败", e);
            log.error("错误详情: ", e);
            return Result.error(500, "添加商品到购物车失败: " + e.getMessage());
        }
    }

    @PutMapping
    public Result<CartDTO> updateCartQuantity(
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            log.info("用户 {} 更新购物车商品 {} 数量为 {}", userId, productId, quantity);
            return Result.success(cartService.updateCartQuantity(userId, productId, quantity));
        } catch (Exception e) {
            log.error("更新购物车商品数量失败", e);
            return Result.error(500, "更新购物车商品数量失败");
        }
    }

    @DeleteMapping
    public Result<Void> removeFromCart(@RequestParam Long productId) {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            log.info("用户 {} 从购物车删除商品 {}", userId, productId);
            cartService.removeFromCart(userId, productId);
            return Result.success();
        } catch (Exception e) {
            log.error("从购物车删除商品失败", e);
            return Result.error(500, "从购物车删除商品失败");
        }
    }

    @DeleteMapping("/clear")
    public Result<Void> clearCart() {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            log.info("用户 {} 清空购物车", userId);
            cartService.clearCart(userId);
            return Result.success();
        } catch (Exception e) {
            log.error("清空购物车失败", e);
            return Result.error(500, "清空购物车失败");
        }
    }

    @GetMapping
    public Result<List<CartDTO>> getUserCart() {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            log.info("获取用户 {} 的购物车数据", userId);
            List<CartDTO> cartItems = cartService.getUserCart(userId);
            log.info("用户 {} 的购物车中有 {} 个商品", userId, cartItems.size());
            return Result.success(cartItems);
        } catch (Exception e) {
            log.error("获取购物车数据失败", e);
            return Result.error(500, "获取购物车数据失败");
        }
    }
} 