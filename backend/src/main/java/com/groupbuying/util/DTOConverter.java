package com.groupbuying.util;

import com.groupbuying.dto.*;
import com.groupbuying.entity.*;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;

@Component
public class DTOConverter {
    
    public UserDTO convertToUserDTO(User user) {
        if (user == null) return null;
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(user, dto);
        return dto;
    }

    public ProductDTO convertToProductDTO(Product product) {
        if (product == null) return null;
        ProductDTO dto = new ProductDTO();
        BeanUtils.copyProperties(product, dto);
        dto.setGroupSize(product.getGroupSize());
        return dto;
    }

    public OrderDTO convertToOrderDTO(Order order) {
        if (order == null) return null;
        OrderDTO dto = new OrderDTO();
        BeanUtils.copyProperties(order, dto);
        dto.setUserId(order.getUser().getId());
        dto.setProductId(order.getProduct().getId());
        dto.setProductName(order.getProduct().getName());
        dto.setProductImageUrl(order.getProduct().getImage());
        return dto;
    }

    public PostDTO convertToPostDTO(Post post) {
        if (post == null) return null;
        PostDTO dto = new PostDTO();
        BeanUtils.copyProperties(post, dto);
        dto.setUserId(post.getUser().getId());
        dto.setUsername(post.getUser().getNickname());
        dto.setUserAvatar(post.getUser().getAvatar());
        if (post.getComments() != null) {
            dto.setComments(post.getComments().stream()
                .map(this::convertToCommentDTO)
                .collect(Collectors.toList()));
        }
        return dto;
    }

    public CommentDTO convertToCommentDTO(Comment comment) {
        if (comment == null) return null;
        CommentDTO dto = new CommentDTO();
        BeanUtils.copyProperties(comment, dto);
        dto.setUserId(comment.getUser().getId());
        dto.setUsername(comment.getUser().getNickname());
        dto.setUserAvatar(comment.getUser().getAvatar());
        
        // 设置父评论ID
        if (comment.getParent() != null) {
            dto.setParentId(comment.getParent().getId());
        }
        
        // 设置回复数量
        dto.setReplyCount(comment.getReplyCount());
        
        // 如果有回复列表，递归转换
        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            dto.setReplies(comment.getReplies().stream()
                .map(this::convertToCommentDTO)
                .collect(Collectors.toList()));
        }
        
        return dto;
    }

   

    public CartDTO convertToCartDTO(Cart cart) {
        if (cart == null) return null;
        CartDTO dto = new CartDTO();
        BeanUtils.copyProperties(cart, dto);
        dto.setUserId(cart.getUser().getId());
        dto.setProductId(cart.getProduct().getId());
        return dto;
    }

    public GroupBuyDTO convertToGroupBuyDTO(GroupBuy groupBuy) {
        if (groupBuy == null) return null;
        
        GroupBuyDTO dto = new GroupBuyDTO();
        BeanUtils.copyProperties(groupBuy, dto);
        
        // 设置产品相关信息
        Product product = groupBuy.getProduct();
        dto.setProductId(product.getId());
        dto.setProductName(product.getName());
        dto.setProductImage(product.getImage());
        dto.setOriginalPrice(product.getPrice());
        dto.setGroupPrice(groupBuy.getGroupPrice());
        
        // 设置团长信息
        User leader = groupBuy.getLeader();
        dto.setLeaderId(leader.getId());
        dto.setLeaderName(leader.getNickname());
        dto.setLeaderAvatar(leader.getAvatar());
        
        // 设置拼团信息
        dto.setRequiredSize(product.getGroupSize());
        dto.setCurrentSize(groupBuy.getCurrentSize());
        dto.setStatus(groupBuy.getStatus().name());
        
        // 设置时间信息
        dto.setCreatedAt(groupBuy.getCreatedAt());
        dto.setExpiryTime(groupBuy.getExpiryTime());
        
        // 计算剩余时间（秒）
        if (groupBuy.getExpiryTime() != null) {
            long remainingSeconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), groupBuy.getExpiryTime());
            dto.setRemainingSeconds(remainingSeconds > 0 ? remainingSeconds : 0);
        }
        
        // 设置参与者信息
        Set<UserDTO> participantDTOs = groupBuy.getParticipants().stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toSet());
        dto.setParticipants(participantDTOs);
        
        return dto;
    }

    public CouponDTO convertToCouponDTO(Coupon coupon) {
        if (coupon == null) {
            return null;
        }
        
        CouponDTO dto = new CouponDTO();
        dto.setId(coupon.getId());
        dto.setUserId(coupon.getUser().getId());
        dto.setDiscount(coupon.getDiscountRate());
        dto.setCreateTime(coupon.getCreateTime());
        dto.setExpiryTime(coupon.getExpiryTime());
        dto.setUsed(coupon.getUsed());
        dto.setDiscountText(String.format("%.0f折", coupon.getDiscountRate() * 100));
        return dto;
    }
} 