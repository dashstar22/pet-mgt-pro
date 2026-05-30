package com.petmgt.controller.user;

import com.petmgt.dto.ApiResponse;
import com.petmgt.service.ai.AiChatService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai-chat")
public class AiChatController {

    private final AiChatService aiChatService;

    public AiChatController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    @PostMapping
    public ApiResponse<Map<String, String>> chat(@RequestBody Map<String, String> body) {
        String question = body.get("question");
        if (question == null || question.trim().isEmpty()) {
            return ApiResponse.error(400, "问题不能为空");
        }
        String answer = aiChatService.chat(question);
        return ApiResponse.success(Map.of("answer", answer));
    }
}
