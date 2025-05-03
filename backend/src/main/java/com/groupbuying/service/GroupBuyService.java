package com.groupbuying.service;

import com.groupbuying.dto.GroupBuyDTO;
import com.groupbuying.entity.GroupBuy;
import java.util.List;

public interface GroupBuyService {
    /**
     * 创建新的拼团
     */
    GroupBuyDTO createGroup(Long productId, Long userId, String paymentPassword);
    
    /**
     * 加入拼团
     */
    GroupBuyDTO joinGroup(String inviteCode, Long userId, String paymentPassword);
    
    /**
     * 根据邀请码获取拼团信息
     */
    GroupBuyDTO getGroupDetails(String inviteCode);
    
    /**
     * 获取用户参与的所有拼团
     */
    List<GroupBuyDTO> getUserGroups(Long userId);
    
    /**
     * 获取商品的可用拼团列表
     */
    List<GroupBuyDTO> getAvailableGroups(Long productId);
    
    /**
     * 处理过期拼团
     */
    void handleExpiredGroupBuys();
    
    /**
     * 检查拼团是否成功
     */
    void checkGroupSuccess(GroupBuy groupBuy);
    
    /**
     * 处理拼团失败的退款
     */
    void processRefund(GroupBuy groupBuy);
} 