package com.example.demo.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController 
@RequestMapping("/api")
public class PromptChatController {

    private final ChatClient chatClient;

    public PromptChatController(ChatClient chatClient){
        this.chatClient = chatClient;
    }



    @Value("classpath:/promptTemplate/userPromptTemplate.st")
    Resource userPromptTemplate;
   
    @GetMapping ("/email")
    public String chat(@RequestParam String customerName, @RequestParam String message){
        return chatClient
            .prompt()
            .system("""
                    You are a professional customer service assistance which helps drafting emnails
                    responses to improve the productivity of the consumer support team. 
                    """)
            .user(promptTemplateSpec -> 
                promptTemplateSpec.text(userPromptTemplate)
                .param("customerName", customerName)
                .param("message", message)
            )
            .call()
            .content();
    }
}
