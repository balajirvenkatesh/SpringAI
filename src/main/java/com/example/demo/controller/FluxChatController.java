package com.example.demo.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;

@RestController 
@RequestMapping("/api")
public class FluxChatController {

     private final ChatClient chatClient;

    public FluxChatController(ChatClient chatClient){
        this.chatClient = chatClient;
    }

    @GetMapping ("/stream")
    public Flux<String> chat( @RequestParam String message){
        return chatClient
            .prompt()
            .user(message)
            .stream()
            .content();
    }
}
