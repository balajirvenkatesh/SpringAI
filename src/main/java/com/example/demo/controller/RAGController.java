package com.example.demo.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController 
@RequestMapping ("/api/rag")
public class RAGController {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    @Value("classpath:/promptTemplate/systemPromptRandomDataTemplate.st")
    Resource promResource;

    RAGController(@Qualifier("chatMemoryClient") ChatClient chatClient, VectorStore vectorStore){
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
    }

    @GetMapping("/random/chat")
    public ResponseEntity<String> randomSearchChat(@RequestParam String username, @RequestParam String message){
        // SearchRequest searchRequest = SearchRequest
        //     .builder()
        //     .query(message)
        //     .topK(3)
        //     .similarityThreshold(0.5)
        //     .build();
        // List<Document> similarDocuments = vectorStore.similaritySearch(searchRequest);
        // similarDocuments.stream()
        // .map(Document::getText)
        // .collect(
        //     Collectors.joining(System.lineSeparator())
        // );
        String answer = chatClient.prompt()
            // .system(promResourceSpec -> promResourceSpec.text(promResource).param("documents", similarDocuments) )
            .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, username))
            .user(message)
            .call()
            .content();
        return ResponseEntity.ok(answer);
    }
}
