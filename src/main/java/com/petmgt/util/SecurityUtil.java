package com.petmgt.util;

import com.petmgt.entity.User;
import com.petmgt.mapper.UserMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {

    private static UserMapper userMapper;

    public SecurityUtil(UserMapper userMapper) {
        SecurityUtil.userMapper = userMapper;
    }

    /**
     * Get the currently authenticated user.
     * With JWT auth, the principal is the userId (Long).
     * Falls back to username-based lookup for non-JWT Authentication types.
     */
    public static User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        Object principal = auth.getPrincipal();

        // JWT auth: principal is userId (Long)
        if (principal instanceof Long userId) {
            return userMapper.selectById(userId);
        }

        // Fallback: principal might be a String (username) or UserDetails
        String username = auth.getName();
        if ("anonymousUser".equals(username)) {
            return null;
        }
        return userMapper.findByUsername(username);
    }
}
