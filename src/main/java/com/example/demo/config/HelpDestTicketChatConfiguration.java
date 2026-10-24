package com.example.demo.config;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import com.example.demo.advisor.TokenUsageAuditAdvisor;
import com.example.demo.rag.TimeTool;

@Configuration 
public class HelpDestTicketChatConfiguration {

    @Value("classpath:/promptTemplate/helpDeskTicketSystemPrompt.st")
    Resource helpDeskTicketSystemPrompt;

    // Same as chatMemoryClient, but the RAG advisor pulls context from a web search instead of the vector store
    @Bean("helpDeskChatClient")
    public ChatClient toolChatClient(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory,
        TimeTool timeTool
    ){
        Advisor loggeAdvisor = new SimpleLoggerAdvisor();
        Advisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
        Advisor tokenUsageAdvisor = new TokenUsageAuditAdvisor();

        return chatClientBuilder
            .defaultSystem(helpDeskTicketSystemPrompt)
            .defaultOptions(OpenAiChatOptions.builder().extraBody(Map.of("include_reasoning", false)))
            .defaultTools(timeTool)
            .defaultAdvisors(List.of(loggeAdvisor, memoryAdvisor, tokenUsageAdvisor))
            .build();
    }
}
