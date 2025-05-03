package com.groupbuying.service.impl;

import com.groupbuying.dto.GroupBuyDTO;
import com.groupbuying.entity.GroupBuy;
import com.groupbuying.entity.Product;
import com.groupbuying.entity.User;
import com.groupbuying.exception.BusinessException;
import com.groupbuying.exception.ErrorCode;
import com.groupbuying.repository.GroupBuyRepository;
import com.groupbuying.repository.ProductRepository;
import com.groupbuying.repository.UserRepository;
import com.groupbuying.service.GroupBuyService;
import com.groupbuying.util.DTOConverter;
import com.groupbuying.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupBuyServiceImpl implements GroupBuyService {
    private final GroupBuyRepository groupBuyRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final DTOConverter dtoConverter;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public GroupBuyDTO createGroup(Long productId, Long userId, String paymentPassword) {
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

        // 计算拼团价格（原价的8折）
        BigDecimal groupPrice = product.getPrice().multiply(new BigDecimal("0.8"));
        
        // 检查用户余额是否足够
        if (user.getAccountBalance().compareTo(groupPrice) < 0) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "账户余额不足");
        }
        
        // 扣减用户余额
        user.setAccountBalance(user.getAccountBalance().subtract(groupPrice));
        userRepository.save(user);

        GroupBuy groupBuy = new GroupBuy();
        groupBuy.setProduct(product);
        groupBuy.setLeader(user);
        groupBuy.setInviteCode(generateInviteCode());
        groupBuy.setRequiredSize(product.getGroupSize());
        groupBuy.setCurrentSize(1);
        groupBuy.setStatus(GroupBuy.Status.IN_PROGRESS);
        groupBuy.setExpiryTime(LocalDateTime.now().plusHours(24));
        groupBuy.getParticipants().add(user);
        groupBuy.setGroupPrice(groupPrice);
        
        // 保存拼团信息
        groupBuy = groupBuyRepository.save(groupBuy);
        
        return dtoConverter.convertToGroupBuyDTO(groupBuy);
    }

    @Override
    @Transactional
    public GroupBuyDTO joinGroup(String inviteCode, Long userId, String paymentPassword) {
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

        GroupBuy groupBuy = groupBuyRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.GROUP_BUY_NOT_FOUND));

        if (groupBuy.getStatus() != GroupBuy.Status.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.GROUP_BUY_NOT_AVAILABLE);
        }

        if (groupBuy.getParticipants().contains(user)) {
            throw new BusinessException(ErrorCode.GROUP_BUY_ALREADY_JOINED);
        }

        // 检查用户余额是否足够
        if (user.getAccountBalance().compareTo(groupBuy.getGroupPrice()) < 0) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "账户余额不足");
        }
        
        // 扣减用户余额
        user.setAccountBalance(user.getAccountBalance().subtract(groupBuy.getGroupPrice()));
        userRepository.save(user);

        groupBuy.getParticipants().add(user);
        groupBuy.setCurrentSize(groupBuy.getCurrentSize() + 1);

        if (groupBuy.getCurrentSize().equals(groupBuy.getRequiredSize())) {
            checkGroupSuccess(groupBuy);
        }

        groupBuyRepository.save(groupBuy);
        return dtoConverter.convertToGroupBuyDTO(groupBuy);
    }

    @Override
    public GroupBuyDTO getGroupDetails(String inviteCode) {
        GroupBuy groupBuy = groupBuyRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.GROUP_BUY_NOT_FOUND));
        return dtoConverter.convertToGroupBuyDTO(groupBuy);
    }

    @Override
    public List<GroupBuyDTO> getUserGroups(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        List<GroupBuy> groupBuys = groupBuyRepository.findByParticipantsContaining(user);
        return groupBuys.stream()
                .map(dtoConverter::convertToGroupBuyDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<GroupBuyDTO> getAvailableGroups(Long productId) {
        List<GroupBuy> groupBuys = groupBuyRepository.findByProductIdAndStatusAndExpiryTimeAfter(
                productId, GroupBuy.Status.IN_PROGRESS, LocalDateTime.now());
        return groupBuys.stream()
                .map(dtoConverter::convertToGroupBuyDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void handleExpiredGroupBuys() {
        List<GroupBuy> expiredGroups = groupBuyRepository.findByStatusAndExpiryTimeBefore(
            GroupBuy.Status.IN_PROGRESS, LocalDateTime.now());

        for (GroupBuy groupBuy : expiredGroups) {
            if (groupBuy.getCurrentSize() < groupBuy.getRequiredSize()) {
                // 拼团失败，更新状态
                groupBuy.setStatus(GroupBuy.Status.FAILED);
                groupBuyRepository.save(groupBuy);
                
                // 处理退款
                processRefund(groupBuy);
            }
        }
    }

    @Override
    @Transactional
    public void checkGroupSuccess(GroupBuy groupBuy) {
        // 只有进行中的拼团才能被标记为成功
        if (groupBuy.getStatus() != GroupBuy.Status.IN_PROGRESS) {
            return;
        }
        
        // 检查是否过期
        if (groupBuy.isExpired()) {
            groupBuy.setStatus(GroupBuy.Status.FAILED);
            groupBuyRepository.save(groupBuy);
            processRefund(groupBuy);
            return;
        }
        
        // 检查人数是否达到要求
        if (groupBuy.getCurrentSize().equals(groupBuy.getRequiredSize())) {
            groupBuy.setStatus(GroupBuy.Status.SUCCESS);
            groupBuyRepository.save(groupBuy);
        }
    }

    @Override
    @Transactional
    public void processRefund(GroupBuy groupBuy) {
        log.info("开始处理拼团退款: groupBuyId={}, status={}", groupBuy.getId(), groupBuy.getStatus());
        
        if (groupBuy.getStatus() != GroupBuy.Status.FAILED) {
            log.warn("拼团状态不是失败状态，不需要退款: groupBuyId={}, status={}", 
                    groupBuy.getId(), groupBuy.getStatus());
            return;
        }

        log.info("拼团参与人数: {}", groupBuy.getParticipants().size());
        for (User participant : groupBuy.getParticipants()) {
            try {
                BigDecimal currentBalance = participant.getAccountBalance();
                BigDecimal newBalance = currentBalance.add(groupBuy.getGroupPrice());
                participant.setAccountBalance(newBalance);
                userRepository.save(participant);
                log.info("退款成功: 用户={}, 金额={}, 新余额={}", 
                        participant.getUsername(), groupBuy.getGroupPrice(), newBalance);
            } catch (Exception e) {
                log.error("退款失败: 用户={}, 金额={}, 原因={}", 
                        participant.getUsername(), groupBuy.getGroupPrice(), e.getMessage());
                log.error("退款异常详情:", e);
            }
        }
        log.info("拼团退款处理完成: groupBuyId={}", groupBuy.getId());
    }

    private String generateInviteCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
} 