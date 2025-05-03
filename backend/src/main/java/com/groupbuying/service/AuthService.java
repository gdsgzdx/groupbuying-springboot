package com.groupbuying.service;

import com.groupbuying.dto.LoginDTO;
import com.groupbuying.dto.RegisterDTO;
import com.groupbuying.dto.UserDTO;

import java.util.Map;

public interface AuthService {
    UserDTO register(RegisterDTO registerDTO);
    Map<String, Object> login(LoginDTO loginDTO);
} 