package com.groupbuying.util;

import com.groupbuying.entity.User;
import com.groupbuying.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityUtil {
    
    private static UserRepository userRepository;
    
    public SecurityUtil(UserRepository userRepository) {
        SecurityUtil.userRepository = userRepository;
    }

    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return userDetails.getUsername();
        }
        return null;
    }

    public static Long getCurrentUserId() {
        String username = getCurrentUsername();
        if (username != null) {
            return userRepository.findByUsername(username)
                    .map(user -> user.getId())
                    .orElse(null);
        }
        return null;
    }

    public static User getCurrentUser() {
        String username = getCurrentUsername();
        if (username != null) {
            return userRepository.findByUsername(username)
                    .orElse(null);
        }
        return null;
    }

    public static void validateUser(Long userId) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null || !currentUserId.equals(userId)) {
            throw new com.groupbuying.exception.BusinessException(com.groupbuying.exception.ErrorCode.UNAUTHORIZED);
        }
    }
} 