package com.petmgt.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petmgt.dto.ApiResponse;
import com.petmgt.dto.ApplicationForm;
import com.petmgt.dto.PageResponse;
import com.petmgt.entity.Application;
import com.petmgt.entity.Pet;
import com.petmgt.entity.User;
import com.petmgt.mapper.*;
import com.petmgt.service.ApplicationService;
import com.petmgt.util.SecurityUtil;
import org.springframework.web.bind.annotation.*;

@RestController("userApplicationController")
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final ApplicationMapper applicationMapper;
    private final PetMapper petMapper;
    private final BreedMapper breedMapper;
    private final PetImageMapper petImageMapper;

    public ApplicationController(ApplicationService applicationService,
                                  ApplicationMapper applicationMapper,
                                  PetMapper petMapper, BreedMapper breedMapper,
                                  PetImageMapper petImageMapper) {
        this.applicationService = applicationService;
        this.applicationMapper = applicationMapper;
        this.petMapper = petMapper;
        this.breedMapper = breedMapper;
        this.petImageMapper = petImageMapper;
    }

    @PostMapping
    public ApiResponse<Void> submit(@RequestBody ApplicationForm form) {
        User user = SecurityUtil.getCurrentUser();
        if (user == null) {
            return ApiResponse.error(401, "未登录");
        }
        applicationService.submit(form, user.getId());
        return ApiResponse.success("申请已提交", null);
    }

    @GetMapping
    public ApiResponse<PageResponse<Application>> myApplications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        User user = SecurityUtil.getCurrentUser();
        if (user == null) {
            return ApiResponse.error(401, "未登录");
        }
        Long userId = user.getId();
        Page<Application> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<Application>()
                .eq(Application::getUserId, userId)
                .orderByDesc(Application::getCreatedAt);
        Page<Application> result = applicationMapper.selectPage(pageParam, wrapper);
        for (Application app : result.getRecords()) {
            Pet pet = petMapper.selectById(app.getPetId());
            if (pet != null) {
                app.setPetName(pet.getName());
                app.setBreedName(breedMapper.selectById(pet.getBreedId()) != null
                        ? breedMapper.selectById(pet.getBreedId()).getBreedName() : null);
            }
        }
        PageResponse<Application> resp = new PageResponse<>(
                result.getRecords(), result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
        return ApiResponse.success(resp);
    }

    @GetMapping("/{id}")
    public ApiResponse<Application> detail(@PathVariable Long id) {
        Application app = applicationMapper.selectById(id);
        if (app == null) {
            return ApiResponse.error(404, "申请不存在");
        }
        return ApiResponse.success(app);
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long id) {
        User user = SecurityUtil.getCurrentUser();
        if (user == null) {
            return ApiResponse.error(401, "未登录");
        }
        applicationService.cancel(id, user.getId());
        return ApiResponse.success("申请已取消", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        User user = SecurityUtil.getCurrentUser();
        if (user == null) {
            return ApiResponse.error(401, "未登录");
        }
        applicationService.deleteById(id, user.getId());
        return ApiResponse.success("删除成功", null);
    }
}
