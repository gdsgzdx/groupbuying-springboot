package com.groupbuying.controller;

import com.groupbuying.common.Result;
import com.groupbuying.dto.LoginDTO;
import com.groupbuying.dto.RegisterDTO;
import com.groupbuying.dto.UserDTO;
import com.groupbuying.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Result<UserDTO> register(@RequestBody RegisterDTO registerDTO) {
        return Result.success(authService.register(registerDTO));
    }

    @PostMapping("/login")
    public Result<Object> login(@RequestBody LoginDTO loginDTO) {
        return Result.success(authService.login(loginDTO));
    }
} 