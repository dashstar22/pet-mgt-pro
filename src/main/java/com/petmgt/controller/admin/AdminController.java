package com.petmgt.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petmgt.dto.ApiResponse;
import com.petmgt.entity.Application;
import com.petmgt.entity.Pet;
import com.petmgt.mapper.ApplicationMapper;
import com.petmgt.mapper.PetMapper;
import com.petmgt.mapper.UserMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserMapper userMapper;
    private final PetMapper petMapper;
    private final ApplicationMapper applicationMapper;

    public AdminController(UserMapper userMapper, PetMapper petMapper,
                           ApplicationMapper applicationMapper) {
        this.userMapper = userMapper;
        this.petMapper = petMapper;
        this.applicationMapper = applicationMapper;
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> stats() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalUsers", userMapper.selectCount(null));
        data.put("totalPets", petMapper.selectCount(null));
        data.put("availablePets", petMapper.selectCount(
                new LambdaQueryWrapper<Pet>().eq(Pet::getStatus, "available")));
        data.put("adoptedPets", petMapper.selectCount(
                new LambdaQueryWrapper<Pet>().eq(Pet::getStatus, "adopted")));
        data.put("pendingApplications", applicationMapper.selectCount(
                new LambdaQueryWrapper<Application>().eq(Application::getStatus, "pending")));
        return ApiResponse.success(data);
    }
}
