package com.petmgt.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petmgt.dto.ApiResponse;
import com.petmgt.dto.PageResponse;
import com.petmgt.entity.AiMatchRecord;
import com.petmgt.mapper.AiMatchRecordMapper;
import com.petmgt.mapper.UserMapper;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/ai-records")
public class AiRecordController {

    private final AiMatchRecordMapper aiMatchRecordMapper;
    private final UserMapper userMapper;

    public AiRecordController(AiMatchRecordMapper aiMatchRecordMapper, UserMapper userMapper) {
        this.aiMatchRecordMapper = aiMatchRecordMapper;
        this.userMapper = userMapper;
    }

    @GetMapping
    public ApiResponse<PageResponse<AiMatchRecord>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<AiMatchRecord> pageParam = new Page<>(page, size);
        Page<AiMatchRecord> result = aiMatchRecordMapper.selectPage(pageParam,
                new LambdaQueryWrapper<AiMatchRecord>().orderByDesc(AiMatchRecord::getCreatedAt));
        for (AiMatchRecord record : result.getRecords()) {
            var user = userMapper.selectById(record.getUserId());
            if (user != null) record.setUsername(user.getUsername());
        }
        PageResponse<AiMatchRecord> resp = new PageResponse<>(
                result.getRecords(), result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
        return ApiResponse.success(resp);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteRecord(@PathVariable Long id) {
        aiMatchRecordMapper.deleteById(id);
        return ApiResponse.success("删除成功", null);
    }
}
