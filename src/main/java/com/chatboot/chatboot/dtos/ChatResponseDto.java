package com.chatboot.chatboot.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponseDto {

    private String response;
    private String conversationId;
    private LocalDateTime timestamp;
    private Integer activeConversations;

    public static ChatResponseDto of(String response, String conversationId, Integer activeConversations) {
        return ChatResponseDto.builder()
                .response(response)
                .conversationId(conversationId)
                .timestamp(LocalDateTime.now())
                .activeConversations(activeConversations)
                .build();
    }
}