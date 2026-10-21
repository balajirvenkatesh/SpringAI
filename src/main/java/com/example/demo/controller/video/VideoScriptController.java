package com.example.demo.controller.video;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/video")
public class VideoScriptController {

    private final ChatClient chatClient;

    VideoScriptController(@Qualifier("videoScriptChatClient") ChatClient chatClient){
        this.chatClient = chatClient;
    }

    // Semantic cache -> web search -> Groq; returns a script to paste into Pixlr's AI Video Generator
    @GetMapping("/script")
    public ResponseEntity<VideoScriptResponse> generateScript(@RequestParam String username, @RequestParam String text){
        String script = chatClient.prompt()
            .user(text)
            .call()
            .content();
        return ResponseEntity.ok(new VideoScriptResponse(username, text, script));
    }

    record VideoScriptResponse(String username, String text, String script) {}
}
