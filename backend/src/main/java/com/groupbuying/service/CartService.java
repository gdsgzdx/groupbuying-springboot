package com.groupbuying.service;

import com.groupbuying.dto.CartDTO;
import java.util.List;

public interface CartService {
    CartDTO addToCart(Long userId, Long productId, Integer quantity);
    List<CartDTO> getUserCart(Long userId);
    CartDTO updateCartQuantity(Long userId, Long productId, Integer quantity);
    void removeFromCart(Long userId, Long productId);
    void clearCart(Long userId);
} 