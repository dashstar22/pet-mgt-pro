package com.petmgt.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petmgt.dto.ApiResponse;
import com.petmgt.dto.PageResponse;
import com.petmgt.entity.Application;
import com.petmgt.entity.Pet;
import com.petmgt.entity.User;
import com.petmgt.mapper.*;
import com.petmgt.service.ApplicationService;
import com.petmgt.service.ai.AiReviewService;
import com.petmgt.util.SecurityUtil;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/applications")
public class ApplicationController {

    private final ApplicationMapper applicationMapper;
    private final ApplicationService applicationService;
    private final PetMapper petMapper;
    private final BreedMapper breedMapper;
    private final PetImageMapper petImageMapper;
    private final UserMapper userMapper;
    private final AiReviewService aiReviewService;

    public ApplicationController(ApplicationMapper applicationMapper,
                                  ApplicationService applicationService,
                                  PetMapper petMapper, BreedMapper breedMapper,
                                  PetImageMapper petImageMapper, UserMapper userMapper,
                                  AiReviewService aiReviewService) {
        this.applicationMapper = applicationMapper;
        this.applicationService = applicationService;
        this.petMapper = petMapper;
        this.breedMapper = breedMapper;
        this.petImageMapper = petImageMapper;
        this.userMapper = userMapper;
        this.aiReviewService = aiReviewService;
    }

    @GetMapping
    public ApiResponse<PageResponse<Application>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        Page<Application> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Application::getStatus, status);
        }
        wrapper.orderByDesc(Application::getCreatedAt);
        Page<Application> result = applicationMapper.selectPage(pageParam, wrapper);
        for (Application app : result.getRecords()) {
            Pet pet = petMapper.selectById(app.getPetId());
            if (pet != null) {
                app.setPetName(pet.getName());
                app.setBreedName(breedMapper.selectById(pet.getBreedId()) != null
                        ? breedMapper.selectById(pet.getBreedId()).getBreedName() : null);
            }
            User user = userMapper.selectById(app.getUserId());
            if (user != null) {
                app.setApplicantUsername(user.getUsername());
            }
        }
        PageResponse<Application> resp = new PageResponse<>(
                result.getRecords(), result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
        return ApiResponse.success(resp);
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        Application app = applicationMapper.selectById(id);
        if (app == null) {
            return ApiResponse.error(404, "申请不存在");
        }
        Pet pet = petMapper.selectById(app.getPetId());
        User user = userMapper.selectById(app.getUserId());
        app.setPetName(pet != null ? pet.getName() : null);
        app.setApplicantUsername(user != null ? user.getUsername() : null);
        if (pet != null) {
            app.setBreedName(breedMapper.selectById(pet.getBreedId()) != null
                    ? breedMapper.selectById(pet.getBreedId()).getBreedName() : null);
        }
        Object aiReview = null;
        if ("pending".equals(app.getStatus())) {
            try {
                aiReview = aiReviewService.review(app, pet);
            } catch (Exception ignored) {}
        }
        Map<String, Object> data = new HashMap<>();
        data.put("application", app);
        data.put("pet", pet);
        data.put("user", user);
        data.put("aiReview", aiReview);
        return ApiResponse.success(data);
    }

    @PutMapping("/{id}/approve")
    public ApiResponse<Void> approve(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Long adminId = SecurityUtil.getCurrentUser().getId();
        String comment = body.getOrDefault("comment", "");
        applicationService.approve(id, adminId, comment);
        return ApiResponse.success("审核通过", null);
    }

    @PutMapping("/{id}/reject")
    public ApiResponse<Void> reject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Long adminId = SecurityUtil.getCurrentUser().getId();
        String reason = body.getOrDefault("reason", "");
        if (reason.isEmpty()) {
            return ApiResponse.error(400, "拒绝原因不能为空");
        }
        applicationService.reject(id, adminId, reason);
        return ApiResponse.success("已拒绝", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        applicationService.adminDeleteById(id);
        return ApiResponse.success("删除成功", null);
    }
}
