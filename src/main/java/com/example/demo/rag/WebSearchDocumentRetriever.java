package com.example.demo.rag;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Plays the same role as VectorStoreDocumentRetriever, but the documents come from a Tavily web search
public class WebSearchDocumentRetriever implements DocumentRetriever {

    private final RestClient restClient;
    private final String apiKey;
    private final int maxResults;

    public WebSearchDocumentRetriever(String apiKey, int maxResults){
        this.apiKey = apiKey;
        this.maxResults = maxResults;
        this.restClient = RestClient.builder()
            .baseUrl("https://api.tavily.com")
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
            .build();
    }

    @Override
    public List<Document> retrieve(Query query) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("TAVILY_API_KEY is not set. Get a key at https://app.tavily.com");
        }

        TavilyResponse response = restClient.post()
            .uri("/search")
            .contentType(MediaType.APPLICATION_JSON)
            .body(Map.of(
                "query", query.text(),
                "max_results", maxResults,
                "search_depth", "basic"))
            .retrieve()
            .body(TavilyResponse.class);

        if (response == null || response.results() == null) {
            return List.of();
        }

        // Each search result becomes a Document: the page snippet is the text, title/url go in metadata
        return response.results().stream()
            .filter(result -> result.content() != null && !result.content().isBlank())
            .map(result -> new Document(result.content(), Map.of(
                "title", Objects.toString(result.title(), ""),
                "url", Objects.toString(result.url(), ""))))
            .toList();
    }

    // Turns the documents into the {context} text, keeping title and url so the model can cite sources
    public static String format(List<Document> documents){
        return documents.stream()
            .map(doc -> "Title: " + doc.getMetadata().get("title")
                + "\nURL: " + doc.getMetadata().get("url")
                + "\nContent: " + doc.getText())
            .collect(Collectors.joining("\n\n"));
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TavilyResponse(List<TavilyResult> results) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TavilyResult(String title, String url, String content) {}
}
