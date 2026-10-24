package com.example.demo.controller;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.rag.HelpDeskTicketTool;

@RestController 
@RequestMapping ("/api/tool")
public class HelpDeskTicketController {

    private final ChatClient chatClient;
    private final HelpDeskTicketTool helpDeskTicketTool;

    HelpDeskTicketController(@Qualifier ("helpDeskChatClient") ChatClient chatClient, HelpDeskTicketTool helpDeskTicketTool){
        this.chatClient = chatClient;
        this.helpDeskTicketTool = helpDeskTicketTool;
    }

    @RequestMapping ("/help-desk")
    public ResponseEntity<String>helpDesk(@RequestParam String username, @RequestParam String message){
        String answer = chatClient.prompt()
        .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, username))
        .user(message)
        .tools(helpDeskTicketTool)
        .toolContext(Map.of("username", username))
        .call()
        .content();
        return ResponseEntity.ok(answer);
    }
}

