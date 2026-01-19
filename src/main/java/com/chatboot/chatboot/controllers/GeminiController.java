package com.chatboot.chatboot.controllers;

import com.chatboot.chatboot.dtos.ChatResponseDto;
import com.chatboot.chatboot.services.MemoryChatBotService;
import com.chatboot.chatboot.services.WhisperService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@RestController
@RequestMapping("/api/gemini")
@CrossOrigin(origins = "*")
public class GeminiController {

    private final MemoryChatBotService chatBotService;
    private final WhisperService whisperService;

    public GeminiController(MemoryChatBotService chatBotService,
                            WhisperService whisperService) {
        this.chatBotService = chatBotService;
        this.whisperService = whisperService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponseDto> chatText(@RequestParam String conversationId,
                                                    @RequestParam String message) {
        String response = chatBotService.sendMessage(message, conversationId);
        return ResponseEntity.ok(ChatResponseDto.of(
                response,
                conversationId,
                chatBotService.getActiveConversationsCount()
        ));
    }

    @PostMapping("/chat/audio")
    public ResponseEntity<ChatResponseDto> chatAudio(@RequestParam String conversationId,
                                                     @RequestParam("file") MultipartFile audioFile) throws Exception {
        File tempFile = File.createTempFile("audio-", ".mp3");
        audioFile.transferTo(tempFile);
        String userMessage = whisperService.transcribe(tempFile);

        String aiResponse = chatBotService.sendMessage(userMessage, conversationId);

        return ResponseEntity.ok(ChatResponseDto.of(
                aiResponse,
                conversationId,
                chatBotService.getActiveConversationsCount()
        ));
    }
}
