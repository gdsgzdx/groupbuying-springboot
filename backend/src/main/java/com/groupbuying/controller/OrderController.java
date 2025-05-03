package com.groupbuying.controller;

import com.groupbuying.common.Result;
import com.groupbuying.dto.CreateOrderRequest;
import com.groupbuying.dto.OrderDTO;
import com.groupbuying.entity.Order;
import com.groupbuying.entity.Order.OrderStatus;
import com.groupbuying.entity.User;
import com.groupbuying.exception.ErrorCode;
import com.groupbuying.service.OrderService;
import com.groupbuying.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@RequestBody CreateOrderRequest request) {
        User currentUser = SecurityUtil.getCurrentUser();
        return ResponseEntity.ok(orderService.createOrder(
            currentUser.getId(), 
            request.getProductId(),
            request.getPaymentPassword(),
            request.getCouponId()
        ));
    }

    @GetMapping("/{orderId}")
    public Result<OrderDTO> getOrder(
            @RequestParam Long userId,
            @PathVariable Long orderId) {
        return Result.success(orderService.getOrder(userId, orderId));
    }

    @GetMapping
    public Result<Page<OrderDTO>> getOrders(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) OrderStatus status,
            Pageable pageable) {
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }
        return Result.success(orderService.getUserOrders(userId, pageable));
    }

    @GetMapping("/status/{status}")
    public Result<Page<OrderDTO>> getUserOrdersByStatus(
            @RequestParam Long userId,
            @PathVariable OrderStatus status,
            Pageable pageable) {
        return Result.success(orderService.getUserOrdersByStatus(userId, status, pageable));
    }

    @PutMapping("/{orderId}/status")
    public Result<OrderDTO> updateOrderStatus(
            @RequestParam Long userId,
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {
        return Result.success(orderService.updateOrderStatus(userId, orderId, status));
    }

    @PostMapping("/{orderId}/cancel")
    public Result<Void> cancelOrder(
            @RequestParam Long userId,
            @PathVariable Long orderId) {
        orderService.cancelOrder(userId, orderId);
        return Result.success();
    }
} 