package com.groupbuying.service.impl;

import com.groupbuying.dto.CouponDTO;
import com.groupbuying.dto.OrderDTO;
import com.groupbuying.entity.Order;
import com.groupbuying.entity.Product;
import com.groupbuying.entity.User;
import com.groupbuying.entity.Order.OrderStatus;
import com.groupbuying.exception.BusinessException;
import com.groupbuying.exception.ErrorCode;
import com.groupbuying.repository.OrderRepository;
import com.groupbuying.repository.ProductRepository;
import com.groupbuying.repository.UserRepository;
import com.groupbuying.service.OrderService;
import com.groupbuying.service.CouponService;
import com.groupbuying.util.DTOConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CouponService couponService;
    private final DTOConverter dtoConverter;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public OrderDTO createOrder(Long userId, Long productId, String paymentPassword, Long couponId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
                
        if (paymentPassword == null || paymentPassword.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "支付密码不能为空");
        }
        
        if (user.getPaymentPassword() == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "请先设置支付密码");
        }
        
        if (!passwordEncoder.matches(paymentPassword, user.getPaymentPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_PASSWORD);
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        // 计算实际支付金额
        BigDecimal actualAmount = product.getPrice();
        Double discountRate = 1.0;
        
        // 如果使用优惠券，先获取折扣率但不标记为已使用
        if (couponId != null) {
            CouponDTO coupon = couponService.getCoupon(couponId, userId);
            if (coupon != null) {
                discountRate = coupon.getDiscount();
                actualAmount = actualAmount.multiply(BigDecimal.valueOf(discountRate));
            }
        }

        // 检查用户余额是否足够
        if (user.getAccountBalance().compareTo(actualAmount) < 0) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_BALANCE);
        }

        // 扣减用户余额
        user.setAccountBalance(user.getAccountBalance().subtract(actualAmount));
        userRepository.save(user);

        // 创建订单
        Order order = new Order();
        order.setUser(user);
        order.setProduct(product);
        order.setQuantity(1);
        order.setTotalAmount(actualAmount);
        order.setStatus(Order.OrderStatus.PAID);
        order = orderRepository.save(order);

        // 订单创建成功后，标记优惠券为已使用
        if (couponId != null) {
            try {
                couponService.useCoupon(couponId, userId);
            } catch (Exception e) {
                log.error("标记优惠券[{}]为已使用时发生错误，但订单已创建成功", couponId, e);
            }
        }

        return dtoConverter.convertToOrderDTO(order);
    }

    @Override
    public OrderDTO getOrder(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        OrderDTO orderDTO = new OrderDTO();
        BeanUtils.copyProperties(order, orderDTO);
        orderDTO.setUserId(order.getUser().getId());
        orderDTO.setProductId(order.getProduct().getId());
        return orderDTO;
    }

    @Override
    public Page<OrderDTO> getUserOrders(Long userId, Pageable pageable) {
        log.info("获取用户订单列表: userId={}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        Page<Order> orders = orderRepository.findByUser(user, pageable);
        
        return orders.map(order -> {
            OrderDTO dto = new OrderDTO();
            BeanUtils.copyProperties(order, dto);
            dto.setUserId(order.getUser().getId());
            dto.setProductId(order.getProduct().getId());
            dto.setProductName(order.getProduct().getName());
            dto.setProductImageUrl(order.getProduct().getImage());
            dto.setStatus(order.getStatus());
            return dto;
        });
    }

    @Override
    public Page<OrderDTO> getUserOrdersByStatus(Long userId, OrderStatus status, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return orderRepository.findByUserAndStatus(user, status, pageable)
                .map(order -> {
                    OrderDTO orderDTO = new OrderDTO();
                    BeanUtils.copyProperties(order, orderDTO);
                    orderDTO.setUserId(order.getUser().getId());
                    orderDTO.setProductId(order.getProduct().getId());
                    return orderDTO;
                });
    }

    @Override
    @Transactional
    public OrderDTO updateOrderStatus(Long userId, Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        order.setStatus(status);
        order = orderRepository.save(order);

        OrderDTO orderDTO = new OrderDTO();
        BeanUtils.copyProperties(order, orderDTO);
        orderDTO.setUserId(order.getUser().getId());
        orderDTO.setProductId(order.getProduct().getId());
        return orderDTO;
    }

    @Override
    @Transactional
    public void cancelOrder(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_STATUS);
        }

        Product product = order.getProduct();
        product.setStock(product.getStock() + order.getQuantity());
        productRepository.save(product);

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }
} 