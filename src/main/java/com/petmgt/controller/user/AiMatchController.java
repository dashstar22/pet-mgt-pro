package com.petmgt.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petmgt.dto.*;
import com.petmgt.entity.AiMatchRecord;
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
        Long userId = SecurityUtil.getCurrentUser().getId();
        Map<String, Object> result = aiMatchService.match(request, userId);
        return ApiResponse.success(result);
    }

    @GetMapping("/history")
    public ApiResponse<PageResponse<AiMatchRecord>> history(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = SecurityUtil.getCurrentUser().getId();
        Page<AiMatchRecord> result = aiMatchService.getUserHistory(userId, page, size);
        PageResponse<AiMatchRecord> resp = new PageResponse<>(
                result.getRecords(), result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
        return ApiResponse.success(resp);
    }

    @DeleteMapping("/history/{id}")
    public ApiResponse<Void> deleteRecord(@PathVariable Long id) {
        // Ownership is verified via the service layer —
        // the record is checked to belong to current user's history
        Long userId = SecurityUtil.getCurrentUser().getId();
        var records = aiMatchService.getUserHistory(userId, 1, 1000).getRecords();
        boolean belongsToUser = records.stream().anyMatch(r -> r.getId().equals(id));
        if (!belongsToUser) {
            return ApiResponse.error(404, "记录不存在");
        }
        aiMatchService.deleteById(id);
        return ApiResponse.success("删除成功", null);
    }
}
