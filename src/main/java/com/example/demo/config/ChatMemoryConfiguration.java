package com.example.demo.config;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration 
public class ChatMemoryConfiguration {

    @Bean("chatMemoryClient")
    public ChatClient chatMemoryClient(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory){
        Advisor loggeAdvisor = new SimpleLoggerAdvisor();
        Advisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
        // gpt-oss on Groq returns its reasoning; Groq rejects it when memory sends it back
        return chatClientBuilder
            .defaultOptions(OpenAiChatOptions.builder().extraBody(Map.of("include_reasoning", false)))
            .defaultAdvisors(List.of(loggeAdvisor, memoryAdvisor))
            .build();
    }
}
