package com.groupbuying.service;

import com.groupbuying.dto.OrderDTO;
import com.groupbuying.entity.Order.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderDTO createOrder(Long userId, Long productId, String paymentPassword, Long couponId);
    OrderDTO getOrder(Long userId, Long orderId);
    Page<OrderDTO> getUserOrders(Long userId, Pageable pageable);
    Page<OrderDTO> getUserOrdersByStatus(Long userId, OrderStatus status, Pageable pageable);
    OrderDTO updateOrderStatus(Long userId, Long orderId, OrderStatus status);
    void cancelOrder(Long userId, Long orderId);
} 