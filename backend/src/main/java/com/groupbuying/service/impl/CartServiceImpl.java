package com.groupbuying.service.impl;

import com.groupbuying.dto.CartDTO;
import com.groupbuying.entity.Cart;
import com.groupbuying.entity.Product;
import com.groupbuying.entity.User;
import com.groupbuying.exception.BusinessException;
import com.groupbuying.exception.ErrorCode;
import com.groupbuying.repository.CartRepository;
import com.groupbuying.repository.ProductRepository;
import com.groupbuying.repository.UserRepository;
import com.groupbuying.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    private CartDTO convertToDTO(Cart cart) {
        CartDTO dto = new CartDTO();
        BeanUtils.copyProperties(cart, dto);
        dto.setUserId(cart.getUser().getId());
        dto.setProductId(cart.getProduct().getId());
        dto.setProductName(cart.getProduct().getName());
        dto.setPrice(cart.getProduct().getPrice());
        dto.setImageUrl(cart.getProduct().getImage());
        return dto;
    }

    @Override
    @Transactional
    public CartDTO addToCart(Long userId, Long productId, Integer quantity) {
        log.info("开始添加商品到购物车: userId={}, productId={}, quantity={}", userId, productId, quantity);
        
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        log.error("用户不存在: userId={}", userId);
                        return new BusinessException(ErrorCode.USER_NOT_FOUND);
                    });

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> {
                        log.error("商品不存在: productId={}", productId);
                        return new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
                    });

            log.info("找到用户和商品: user={}, product={}", user.getUsername(), product.getName());

            if (product.getStock() < quantity) {
                log.error("商品库存不足: productId={}, stock={}, quantity={}", productId, product.getStock(), quantity);
                throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK);
            }

            Cart existingCart = cartRepository.findByUserIdAndProductId(userId, productId);
            if (existingCart != null) {
                log.info("购物车中已存在该商品，更新数量");
                existingCart.setQuantity(existingCart.getQuantity() + quantity);
                return convertToDTO(cartRepository.save(existingCart));
            }

            log.info("创建新的购物车项");
            Cart cart = new Cart();
            cart.setUser(user);
            cart.setProduct(product);
            cart.setQuantity(quantity);
            Cart savedCart = cartRepository.save(cart);
            log.info("购物车项创建成功: {}", savedCart);
            return convertToDTO(savedCart);
        } catch (Exception e) {
            log.error("添加商品到购物车时发生错误", e);
            throw e;
        }
    }

    @Override
    public List<CartDTO> getUserCart(Long userId) {
        log.info("获取用户购物车: userId={}", userId);
        List<Cart> cartItems = cartRepository.findByUserId(userId);
        log.info("找到 {} 个购物车项", cartItems.size());
        return cartItems.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CartDTO updateCartQuantity(Long userId, Long productId, Integer quantity) {
        log.info("更新购物车商品数量: userId={}, productId={}, quantity={}", userId, productId, quantity);
        
        Cart cart = cartRepository.findByUserIdAndProductId(userId, productId);
        if (cart == null) {
            log.error("购物车项不存在: userId={}, productId={}", userId, productId);
            throw new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND);
        }

        if (cart.getProduct().getStock() < quantity) {
            log.error("商品库存不足: productId={}, stock={}, quantity={}", productId, cart.getProduct().getStock(), quantity);
            throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK);
        }

        cart.setQuantity(quantity);
        return convertToDTO(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public void removeFromCart(Long userId, Long productId) {
        log.info("从购物车删除商品: userId={}, productId={}", userId, productId);
        
        Cart cart = cartRepository.findByUserIdAndProductId(userId, productId);
        if (cart == null) {
            log.error("购物车项不存在: userId={}, productId={}", userId, productId);
            throw new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND);
        }
        cartRepository.delete(cart);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        log.info("清空用户购物车: userId={}", userId);
        cartRepository.deleteByUserId(userId);
    }
} 