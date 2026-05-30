package com.petmgt.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petmgt.dto.ApiResponse;
import com.petmgt.dto.PageResponse;
import com.petmgt.entity.User;
import com.petmgt.mapper.RoleMapper;
import com.petmgt.mapper.UserMapper;
import com.petmgt.util.SecurityUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
public class UserController {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserMapper userMapper, RoleMapper roleMapper,
                          PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public ApiResponse<PageResponse<User>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<User> pageParam = new Page<>(page, size);
        Page<User> result = userMapper.selectPage(pageParam,
                new LambdaQueryWrapper<User>().orderByDesc(User::getCreatedAt));
        result.getRecords().forEach(u -> u.setPassword(null));
        PageResponse<User> resp = new PageResponse<>(
                result.getRecords(), result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
        return ApiResponse.success(resp);
    }

    @GetMapping("/{id}")
    public ApiResponse<User> getById(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user != null) user.setPassword(null);
        return ApiResponse.success(user);
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody Map<String, Object> body) {
        String username = (String) body.get("username");
        String password = (String) body.get("password");
        String email = (String) body.get("email");
        @SuppressWarnings("unchecked")
        List<Integer> roleIds = (List<Integer>) body.get("roleIds");

        if (username == null || password == null) {
            return ApiResponse.error(400, "用户名和密码不能为空");
        }
        User existing = userMapper.findByUsername(username);
        if (existing != null) {
            return ApiResponse.error(400, "用户名已存在");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setEnabled(1);
        userMapper.insert(user);
        if (roleIds != null) {
            for (Integer roleId : roleIds) {
                roleMapper.insertUserRole(user.getId(), roleId.longValue());
            }
        }
        return ApiResponse.success("创建成功", null);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        User currentUser = SecurityUtil.getCurrentUser();
        if (currentUser != null && currentUser.getId().equals(id)) {
            return ApiResponse.error(400, "不能编辑自己的账号");
        }
        User user = userMapper.selectById(id);
        if (user == null) {
            return ApiResponse.error(404, "用户不存在");
        }
        if (body.containsKey("email")) {
            user.setEmail((String) body.get("email"));
        }
        if (body.containsKey("enabled")) {
            user.setEnabled(((Number) body.get("enabled")).intValue());
        }
        if (body.containsKey("password") && body.get("password") != null
                && !((String) body.get("password")).isEmpty()) {
            user.setPassword(passwordEncoder.encode((String) body.get("password")));
        }
        userMapper.updateById(user);
        return ApiResponse.success("更新成功", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        User currentUser = SecurityUtil.getCurrentUser();
        if (currentUser != null && currentUser.getId().equals(id)) {
            return ApiResponse.error(400, "不能删除自己的账号");
        }
        userMapper.deleteById(id);
        return ApiResponse.success("删除成功", null);
    }
}
