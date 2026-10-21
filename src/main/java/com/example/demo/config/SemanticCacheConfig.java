package com.example.demo.config;

import org.springframework.ai.chat.cache.semantic.SemanticCache;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.vectorstore.redis.cache.semantic.DefaultSemanticCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import redis.clients.jedis.RedisClient;

@Configuration
public class SemanticCacheConfig {

    @Bean
    RedisClient redisClient(@Value("${spring.data.redis.host}") String host,
                            @Value("${spring.data.redis.port}") int port){
        return RedisClient.builder()
            .hostAndPort(host, port)
            .build();
    }

    // Stores answers in Redis keyed by the embedding of the user's text,
    // so a similar question returns the saved answer without calling the model again
    @Bean
    SemanticCache semanticCache(RedisClient redisClient, EmbeddingModel embeddingModel,
                                @Value("${spring.ai.google.genai.embedding.text.dimensions}") int dimensions){
        // The Gemini model reports its default size (3072) from dimensions(), not the configured 768,
        // and the cache creates its Redis index with that number, so report the real size here
        EmbeddingModel cacheEmbeddingModel = new EmbeddingModel() {
            @Override
            public EmbeddingResponse call(EmbeddingRequest request) {
                return embeddingModel.call(request);
            }

            @Override
            public float[] embed(Document document) {
                return embeddingModel.embed(document);
            }

            @Override
            public int dimensions() {
                return dimensions;
            }
        };
        return DefaultSemanticCache.builder()
            .jedisClient(redisClient)
            .embeddingModel(cacheEmbeddingModel)
            .indexName("video-script-cache")
            .prefix("video-script:")
            .similarityThreshold(0.95)
            .build();
    }
}
