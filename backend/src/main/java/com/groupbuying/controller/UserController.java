package com.groupbuying.controller;

import com.groupbuying.common.Result;
import com.groupbuying.dto.UserDTO;
import com.groupbuying.entity.User;
import com.groupbuying.exception.BusinessException;
import com.groupbuying.exception.ErrorCode;
import com.groupbuying.repository.UserRepository;
import com.groupbuying.util.DTOConverter;
import com.groupbuying.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserRepository userRepository;
    private final DTOConverter dtoConverter;
    private final PasswordEncoder passwordEncoder;

    @Value("${file.upload-dir}")
    private String uploadDir;

    private static final String DEFAULT_AVATAR = "/assets/avatar.png";

    @PostConstruct
    public void init() {
        try {
            // 创建上传目录
            File baseDir = new File(uploadDir);
            File avatarDir = new File(baseDir, "avatars");
            if (!avatarDir.exists()) {
                boolean created = avatarDir.mkdirs();
                if (created) {
                    log.info("创建头像上传目录成功：{}", avatarDir.getAbsolutePath());
                } else {
                    log.error("创建头像上传目录失败：{}", avatarDir.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            log.error("初始化上传目录失败", e);
        }
    }

    @GetMapping("/profile")
    public Result<UserDTO> getProfile() {
        String username = SecurityUtil.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        // 如果用户没有头像，设置默认头像
        if (user.getAvatar() == null || user.getAvatar().isEmpty()) {
            user.setAvatar(DEFAULT_AVATAR);
            userRepository.save(user);
        }
        
        return Result.success(dtoConverter.convertToUserDTO(user));
    }

    @PutMapping("/profile")
    public Result<UserDTO> updateProfile(@RequestBody UserDTO userDTO) {
        String username = SecurityUtil.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (userDTO.getNickname() != null) {
            user.setNickname(userDTO.getNickname());
        }
        if (userDTO.getPhone() != null) {
            user.setPhone(userDTO.getPhone());
        }
        if (userDTO.getAddress() != null) {
            user.setAddress(userDTO.getAddress());
        }
        if (userDTO.getAvatar() != null) {
            user.setAvatar(userDTO.getAvatar());
        }

        user = userRepository.save(user);
        return Result.success(dtoConverter.convertToUserDTO(user));
    }

    @PostMapping("/upload-avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            log.error("上传的文件为空");
            throw new BusinessException(ErrorCode.FILE_EMPTY);
        }

        try {
            // 获取文件名和扩展名
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                log.error("原始文件名为空");
                throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, "文件名无效");
            }

            // 验证文件类型
            String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            if (!Arrays.asList(".jpg", ".jpeg", ".png", ".gif").contains(extension)) {
                log.error("不支持的文件类型：{}", extension);
                throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, "只支持jpg、jpeg、png、gif格式的图片");
            }

            // 生成新文件名
            String newFilename = UUID.randomUUID().toString() + extension;
            
            // 构建文件保存路径
            File avatarDir = new File(uploadDir, "avatars");
            if (!avatarDir.exists() && !avatarDir.mkdirs()) {
                log.error("创建头像目录失败：{}", avatarDir.getAbsolutePath());
                throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, "无法创建上传目录");
            }

            // 保存文件
            File destFile = new File(avatarDir, newFilename);
            file.transferTo(destFile);
            log.info("文件上传成功：{}", destFile.getAbsolutePath());

            // 更新用户头像
            String avatarUrl = "/uploads/avatars/" + newFilename;
            String username = SecurityUtil.getCurrentUsername();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            user.setAvatar(avatarUrl);
            userRepository.save(user);
            log.info("用户头像更新成功：{}", username);

            return Result.success(avatarUrl);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, e.getMessage());
        }
    }

    @PostMapping("/security-question")
    public Result<Void> setSecurityQuestion(@RequestBody Map<String, String> request) {
        String username = SecurityUtil.getCurrentUsername();
        if (username == null) {
            return Result.error(ErrorCode.UNAUTHORIZED.getCode(), "请先登录");
        }

        String question = request.get("question");
        String answer = request.get("answer");

        if (question == null || answer == null) {
            return Result.error(ErrorCode.INVALID_PARAMETER.getCode(), "密保问题和答案不能为空");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.setSecurityQuestion(question);
        user.setSecurityAnswer(answer);
        userRepository.save(user);

        return Result.success();
    }

    @GetMapping("/security-question/{username}")
    public Result<String> getSecurityQuestion(@PathVariable String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getSecurityQuestion() == null) {
            return Result.error(ErrorCode.NOT_FOUND.getCode(), "该用户未设置密保问题");
        }

        return Result.success(user.getSecurityQuestion());
    }

    @PostMapping("/verify-security-answer")
    public Result<Void> verifySecurityAnswer(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String answer = request.get("answer");

        if (username == null || answer == null) {
            return Result.error(ErrorCode.INVALID_PARAMETER.getCode(), "用户名和密保答案不能为空");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!answer.equals(user.getSecurityAnswer())) {
            return Result.error(ErrorCode.INVALID_PARAMETER.getCode(), "密保答案错误");
        }

        return Result.success();
    }

    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String newPassword = request.get("newPassword");
        String securityAnswer = request.get("securityAnswer");

        if (username == null || newPassword == null || securityAnswer == null) {
            return Result.error(ErrorCode.INVALID_PARAMETER.getCode(), "参数不完整");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!securityAnswer.equals(user.getSecurityAnswer())) {
            return Result.error(ErrorCode.INVALID_PARAMETER.getCode(), "密保答案错误");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return Result.success();
    }

    @GetMapping("/balance")
    public Result<BigDecimal> getBalance() {
        String username = SecurityUtil.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return Result.success(user.getAccountBalance());
    }
} 