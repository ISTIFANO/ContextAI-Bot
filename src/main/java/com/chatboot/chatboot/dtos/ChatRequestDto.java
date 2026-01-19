package com.chatboot.chatboot.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequestDto {

    private String message;

    private String conversationId;

    public String getConversationId() {
        return conversationId != null ? conversationId : "default";
    }
}