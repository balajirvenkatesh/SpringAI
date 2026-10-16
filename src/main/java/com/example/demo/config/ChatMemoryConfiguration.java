package com.example.demo.config;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import com.example.demo.rag.WebSearchDocumentRetriever;

@Configuration 
public class ChatMemoryConfiguration {

    @Bean("chatMemoryClient")
    public ChatClient chatMemoryClient(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory, RetrievalAugmentationAdvisor retrievalAugmentationAdvisor){
        Advisor loggeAdvisor = new SimpleLoggerAdvisor();
        Advisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
        // gpt-oss on Groq returns its reasoning; Groq rejects it when memory sends it back
        return chatClientBuilder
            .defaultOptions(OpenAiChatOptions.builder().extraBody(Map.of("include_reasoning", false)))
            .defaultAdvisors(List.of(loggeAdvisor, memoryAdvisor, retrievalAugmentationAdvisor))
            .build();
    }

    // Same as chatMemoryClient, but the RAG advisor pulls context from a web search instead of the vector store
    @Bean("webSearchChatClient")
    public ChatClient webSearchChatClient(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory,
                                          @Value("${tavily.api-key}") String tavilyApiKey,
                                          @Value("classpath:/promptTemplate/webSearchPromptTemplate.st") Resource webSearchPrompt){
        Advisor loggeAdvisor = new SimpleLoggerAdvisor();
        Advisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
        // Not a @Bean: a second RetrievalAugmentationAdvisor bean would make chatMemoryClient's injection ambiguous
        Advisor webSearchAdvisor = RetrievalAugmentationAdvisor.builder()
            .documentRetriever(new WebSearchDocumentRetriever(tavilyApiKey, 5))
            .queryAugmenter(ContextualQueryAugmenter.builder()
                    .promptTemplate(new PromptTemplate(webSearchPrompt))
                    .documentFormatter(WebSearchDocumentRetriever::format)
                    .build()
                )
            .build();
        return chatClientBuilder
            .defaultOptions(OpenAiChatOptions.builder().extraBody(Map.of("include_reasoning", false)))
            .defaultAdvisors(List.of(loggeAdvisor, memoryAdvisor, webSearchAdvisor))
            .build();
    }

    @Bean
    RetrievalAugmentationAdvisor retrievalAugmentationAdvisor (VectorStore vectorStore){
        return RetrievalAugmentationAdvisor.builder()
            .documentRetriever(VectorStoreDocumentRetriever.builder()
                    .vectorStore(vectorStore)
                    .topK(3)
                    .similarityThreshold(0.5)
                    .build()
                )
            .build();
    }
}
