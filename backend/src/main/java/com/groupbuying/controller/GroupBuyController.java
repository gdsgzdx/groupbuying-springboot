package com.groupbuying.controller;

import com.groupbuying.common.Result;
import com.groupbuying.dto.GroupBuyDTO;
import com.groupbuying.service.GroupBuyService;
import com.groupbuying.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import lombok.Data;

import java.util.List;

@RestController
@RequestMapping("/api/group-buys")
@RequiredArgsConstructor
public class GroupBuyController {
    private final GroupBuyService groupBuyService;

    @Data
    public static class CreateGroupRequest {
        private Long productId;
        private String paymentPassword;
    }

    @Data
    public static class JoinGroupRequest {
        private String inviteCode;
        private String paymentPassword;
    }

    @PostMapping("/create")
    public Result<GroupBuyDTO> createGroup(@RequestBody CreateGroupRequest request) {
        Long userId = SecurityUtil.getCurrentUser().getId();
        return Result.success(groupBuyService.createGroup(request.getProductId(), userId, request.getPaymentPassword()));
    }

    @PostMapping("/join")
    public Result<GroupBuyDTO> joinGroup(@RequestBody JoinGroupRequest request) {
        Long userId = SecurityUtil.getCurrentUser().getId();
        return Result.success(groupBuyService.joinGroup(request.getInviteCode(), userId, request.getPaymentPassword()));
    }

    @GetMapping("/{inviteCode}")
    public Result<GroupBuyDTO> getGroupDetails(@PathVariable String inviteCode) {
        return Result.success(groupBuyService.getGroupDetails(inviteCode));
    }

    @GetMapping("/user")
    public Result<List<GroupBuyDTO>> getUserGroups() {
        Long userId = SecurityUtil.getCurrentUser().getId();
        return Result.success(groupBuyService.getUserGroups(userId));
    }

    @GetMapping("/available")
    public Result<List<GroupBuyDTO>> getAvailableGroups(@RequestParam Long productId) {
        return Result.success(groupBuyService.getAvailableGroups(productId));
    }
} 