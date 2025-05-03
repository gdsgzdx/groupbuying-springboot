package com.groupbuying.repository;

import com.groupbuying.entity.Order;
import com.groupbuying.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUser(User user, Pageable pageable);
    Page<Order> findByUserAndStatus(User user, Order.OrderStatus status, Pageable pageable);
} 