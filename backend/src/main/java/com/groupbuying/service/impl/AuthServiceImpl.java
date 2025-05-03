package com.groupbuying.service.impl;

import com.groupbuying.dto.LoginDTO;
import com.groupbuying.dto.RegisterDTO;
import com.groupbuying.dto.UserDTO;
import com.groupbuying.entity.User;
import com.groupbuying.exception.BusinessException;
import com.groupbuying.exception.ErrorCode;
import com.groupbuying.repository.UserRepository;
import com.groupbuying.security.JwtTokenProvider;
import com.groupbuying.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public UserDTO register(RegisterDTO registerDTO) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(registerDTO.getUsername())) {
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        // 创建新用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setNickname(registerDTO.getNickname());
        user.setPaymentPassword(passwordEncoder.encode(registerDTO.getPaymentPassword()));
        user.setActive(true);
        // 设置默认账户余额为50000
        user.setAccountBalance(new BigDecimal("50000"));

        user = userRepository.save(user);

        // 转换为DTO并返回
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        return userDTO;
    }

    @Override
    public Map<String, Object> login(LoginDTO loginDTO) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginDTO.getUsername(),
                    loginDTO.getPassword()
                )
            );

            String token = jwtTokenProvider.createToken(authentication.getName());
            
            // 获取用户信息
            User user = userRepository.findByUsername(loginDTO.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            
            UserDTO userDTO = new UserDTO();
            BeanUtils.copyProperties(user, userDTO);

            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("user", userDTO);
            
            return result;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
    }
} 