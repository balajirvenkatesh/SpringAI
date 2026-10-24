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
@RequestMapping("/api/tool")
public class TimeToolChatController {

    private final ChatClient toolChatClient;

    TimeToolChatController(@Qualifier("toolChatClient") ChatClient toolChatClient){
        this.toolChatClient = toolChatClient;
    }

    @GetMapping("/local-time")
    public ResponseEntity<String> chatMemory(@RequestParam String userName, @RequestParam String message){
        return ResponseEntity.ok(toolChatClient
            .prompt()
            .user(message)
            .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, userName))
            .call()
            .content()
        );
    }
}
