package com.petmgt.controller;

import com.petmgt.dto.ApiResponse;
import com.petmgt.dto.LoginRequest;
import com.petmgt.dto.LoginResponse;
import com.petmgt.dto.RegisterForm;
import com.petmgt.entity.User;
import com.petmgt.mapper.RoleMapper;
import com.petmgt.mapper.UserMapper;
import com.petmgt.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserService userService, UserMapper userMapper,
                          RoleMapper roleMapper, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ApiResponse<Void> register(@RequestBody RegisterForm form) {
        userService.register(form);
        return ApiResponse.success("注册成功，请登录", null);
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ApiResponse.error(401, "用户名或密码错误");
        }
        if (user.getEnabled() != null && user.getEnabled() == 0) {
            return ApiResponse.error(403, "账号已被禁用");
        }
        List<String> roles = roleMapper.findRoleNamesByUserId(user.getId());
        LoginResponse resp = new LoginResponse(null, user.getUsername(), roles);
        return ApiResponse.success(resp);
    }
}
