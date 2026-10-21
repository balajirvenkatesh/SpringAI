package com.example.demo.config;

import java.util.List;

import org.springframework.ai.chat.cache.semantic.SemanticCache;
import org.springframework.ai.chat.cache.semantic.SemanticCacheAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import com.example.demo.rag.WebSearchDocumentRetriever;

// Turns a user's idea into a video script for a text-to-video tool (e.g. Pixlr's AI Video Generator).
// Pixlr has no public API, so this produces the script/prompt; the video itself is made on Pixlr's website.
@Configuration
public class VideoScriptConfiguration {

    @Bean("videoScriptChatClient")
    public ChatClient videoScriptChatClient(ChatClient.Builder chatClientBuilder, SemanticCache semanticCache,
                                            @Value("${tavily.api-key}") String tavilyApiKey,
                                            @Value("classpath:/promptTemplate/videoScriptSystemPrompt.st") Resource systemPrompt,
                                            @Value("classpath:/promptTemplate/videoScriptWebSearchTemplate.st") Resource webSearchPrompt){
        // Runs first: on a cache hit, the web search and the model call are both skipped
        Advisor cacheAdvisor = SemanticCacheAdvisor.builder()
            .cache(semanticCache)
            .build();
        // Adds live web results to the prompt so the script uses current facts
        Advisor webSearchAdvisor = RetrievalAugmentationAdvisor.builder()
            .documentRetriever(new WebSearchDocumentRetriever(tavilyApiKey, 5))
            .queryAugmenter(ContextualQueryAugmenter.builder()
                    .promptTemplate(new PromptTemplate(webSearchPrompt))
                    .documentFormatter(WebSearchDocumentRetriever::format)
                    // Still write a script when the search finds nothing
                    .allowEmptyContext(true)
                    .build()
                )
            .build();
        return chatClientBuilder
            .defaultSystem(systemPrompt)
            .defaultAdvisors(List.of(new SimpleLoggerAdvisor(), cacheAdvisor, webSearchAdvisor))
            .build();
    }
}
