package com.chatboot.chatboot.controllers;

import com.chatboot.chatboot.dtos.ChatRequestDto;
import com.chatboot.chatboot.dtos.ChatResponseDto;
import com.chatboot.chatboot.services.MemoryChatBotService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gemini")
@CrossOrigin(origins = "*")
public class GeminiController {

    private final MemoryChatBotService chatBotService;

    public GeminiController(MemoryChatBotService chatBotService) {
        this.chatBotService = chatBotService;
    }


    @PostMapping("/chat")
    public ResponseEntity<ChatResponseDto> chat(@Valid @RequestBody ChatRequestDto request) {
        String conversationId = request.getConversationId();
        String response = chatBotService.sendMessage(request.getMessage(), conversationId);

        ChatResponseDto responseDto = ChatResponseDto.of(
                response,
                conversationId,
                chatBotService.getActiveConversationsCount()
        );

        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/conversation/{conversationId}")
    public ResponseEntity<String> clearConversation(@PathVariable String conversationId) {
        chatBotService.clearConversation(conversationId);
        return ResponseEntity.ok("removed succ : "  + conversationId);
    }

    @GetMapping("/stats")
    public ResponseEntity<Integer> getStats() {
        return ResponseEntity.ok(chatBotService.getActiveConversationsCount());
    }
}