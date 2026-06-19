package com.petmgt.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petmgt.dto.*;
import com.petmgt.entity.AiMatchRecord;
import com.petmgt.entity.User;
import com.petmgt.service.ai.AiMatchService;
import com.petmgt.util.SecurityUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai-match")
public class AiMatchController {

    private final AiMatchService aiMatchService;

    public AiMatchController(AiMatchService aiMatchService) {
        this.aiMatchService = aiMatchService;
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> match(@RequestBody AiMatchRequest request) {
        User user = SecurityUtil.getCurrentUser();
        if (user == null) {
            return ApiResponse.error(401, "未登录");
        }
        Map<String, Object> result = aiMatchService.match(request, user.getId());
        return ApiResponse.success(result);
    }

    @GetMapping("/history")
    public ApiResponse<PageResponse<AiMatchRecord>> history(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        User user = SecurityUtil.getCurrentUser();
        if (user == null) {
            return ApiResponse.error(401, "未登录");
        }
        Page<AiMatchRecord> result = aiMatchService.getUserHistory(user.getId(), page, size);
        PageResponse<AiMatchRecord> resp = new PageResponse<>(
                result.getRecords(), result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
        return ApiResponse.success(resp);
    }

    @DeleteMapping("/history/{id}")
    public ApiResponse<Void> deleteRecord(@PathVariable Long id) {
        User user = SecurityUtil.getCurrentUser();
        if (user == null) {
            return ApiResponse.error(401, "未登录");
        }
        // Ownership is verified via the service layer —
        // the record is checked to belong to current user's history
        var records = aiMatchService.getUserHistory(user.getId(), 1, 1000).getRecords();
        boolean belongsToUser = records.stream().anyMatch(r -> r.getId().equals(id));
        if (!belongsToUser) {
            return ApiResponse.error(404, "记录不存在");
        }
        aiMatchService.deleteById(id);
        return ApiResponse.success("删除成功", null);
    }
}
