package com.example.demo.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController 
@RequestMapping("/api")
public class ChatMemoryContoller {

    private final ChatClient chatClient;

    ChatMemoryContoller(@Qualifier("chatMemoryClient") ChatClient chatClient){
        this.chatClient = chatClient;
    }

    @GetMapping("/chat-memory")
    public ResponseEntity<String> chatMemory(@RequestParam String userName, @RequestParam String message){
        return ResponseEntity.ok(chatClient
            .prompt()
            .user(message)
            .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, userName))
            .call()
            .content()
        );
    }
}
