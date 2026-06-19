package com.petmgt.controller.user;

import com.petmgt.dto.ApiResponse;
import com.petmgt.entity.User;
import com.petmgt.mapper.RoleMapper;
import com.petmgt.mapper.UserMapper;
import com.petmgt.util.SecurityUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class ProfileController {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleMapper roleMapper;

    public ProfileController(UserMapper userMapper, PasswordEncoder passwordEncoder,
                             RoleMapper roleMapper) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.roleMapper = roleMapper;
    }

    @GetMapping("/profile")
    public ApiResponse<User> profile() {
        User user = SecurityUtil.getCurrentUser();
        if (user == null) {
            return ApiResponse.error(401, "未登录");
        }
        // Populate roles so the frontend can restore isAdmin on page refresh
        List<String> roles = roleMapper.findRoleNamesByUserId(user.getId());
        user.setRoles(roles);
        user.setPassword(null);
        return ApiResponse.success(user);
    }

    @PutMapping("/profile")
    public ApiResponse<Void> updateProfile(@RequestBody Map<String, String> body) {
        User user = SecurityUtil.getCurrentUser();
        if (user == null) {
            return ApiResponse.error(401, "未登录");
        }
        if (body.containsKey("email")) {
            user.setEmail(body.get("email"));
        }
        if (body.containsKey("avatarUrl")) {
            user.setAvatarUrl(body.get("avatarUrl"));
        }
        userMapper.updateById(user);
        return ApiResponse.success("更新成功", null);
    }

    @PutMapping("/password")
    public ApiResponse<Void> changePassword(@RequestBody Map<String, String> body) {
        User user = SecurityUtil.getCurrentUser();
        if (user == null) {
            return ApiResponse.error(401, "未登录");
        }
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        if (oldPassword == null || newPassword == null) {
            return ApiResponse.error(400, "旧密码和新密码不能为空");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return ApiResponse.error(400, "旧密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        return ApiResponse.success("密码修改成功", null);
    }
}
