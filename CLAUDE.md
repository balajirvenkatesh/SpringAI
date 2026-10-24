# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

A Spring AI learning/demo app (Spring Boot 4.1, Spring AI 2.0, Java 17, Maven, Lombok). It exposes REST endpoints that each show a different Spring AI feature: plain chat, prompt templates, streaming, chat memory, RAG over a vector store, RAG over web search, tool calling, and a semantic cache.

## Commands

Run all commands from the repository root:

```bash
./mvnw spring-boot:run                                   # run the app (port 8080)
./mvnw clean package                                     # build the jar (runs tests)
./mvnw clean package -DskipTests                         # build without tests
./mvnw test                                              # all tests
./mvnw test -Dtest=DemoApplicationTests                  # one test class
./mvnw test -Dtest=DemoApplicationTests#contextLoads     # one test method
```

The only test is `contextLoads`, which starts the full context. It needs the same infrastructure as running the app.

## Runtime dependencies

- **Docker:** `spring-boot-docker-compose` starts `compose.yaml` (Qdrant on 6333/6334 and Redis on 6379) on app startup, and runs `down` on shutdown. Docker must be running.
- **MySQL:** must be running locally on 3306 (`root` user). It is **not** in `compose.yaml`. The database `spring_ai_db` is created automatically. JPA uses `ddl-auto=update`, and the Spring AI chat-memory table is created at startup.
- **External APIs** (keys come from the `GROQ_API_KEY`, `GEMINI_API_KEY` and `TAVILY_API_KEY` environment variables; `application.properties` only references them):
  - Chat: Groq via the OpenAI starter (`base-url=https://api.groq.com/openai/v1`, model `openai/gpt-oss-120b`).
  - Embeddings: Google GenAI `gemini-embedding-001` at **768 dimensions**. OpenAI embeddings are disabled on purpose (`spring.ai.model.embedding=none`), because Gemini's OpenAI-compatible endpoint breaks the OpenAI SDK.
  - Web search: Tavily (`tavily.api-key`).

## Architecture

Each feature has its own named `ChatClient` bean in `config/`, built from the prototype `ChatClient.Builder` with its own advisors, tools and system prompt. Each controller injects one client with `@Qualifier`:

| Bean | Config | Advisors / tools | Endpoint(s) |
|---|---|---|---|
| `chatClient` | `ChatClientConfiguration` | HR system prompt | `/api/stream`, `/api/email` |
| (builder-built in controller) | — | none | `/api/chat` |
| `chatMemoryClient` | `ChatMemoryConfiguration` | logger, JDBC memory, vector-store RAG (Qdrant, topK 3, threshold 0.5) | `/api/chat-memory`, `/api/rag/random/chat` |
| `webSearchChatClient` | `ChatMemoryConfiguration` | logger, memory, Tavily RAG | `/api/rag/web/chat` |
| `toolChatClient` | `TimeToolChatConfiguration` | logger, memory, token audit, `TimeTool` | `/api/tool/local-time` |
| `helpDeskChatClient` | `HelpDestTicketChatConfiguration` | logger, memory, token audit, `TimeTool`, help-desk system prompt | `/api/tool/help-desk` |
| `videoScriptChatClient` | `VideoScriptConfiguration` | logger, Redis semantic cache, Tavily RAG | `/api/video/script` |

Key conventions and gotchas:

- **Unqualified `ChatClient` injection** (`FluxChatController`, `PromptChatController`) resolves to the `chatClient` bean only because the constructor parameter is named `chatClient`. Renaming that parameter, or adding a `@Primary`, changes which client those controllers get.
- **Only one `RetrievalAugmentationAdvisor` may be a bean.** `chatMemoryClient` injects it by type. Any other RAG advisor (such as the web-search ones) must be built inline, not declared as a `@Bean`.
- **Groq + chat memory:** every client that uses memory sets `OpenAiChatOptions.extraBody(Map.of("include_reasoning", false))`. Without it, gpt-oss reasoning gets stored and Groq rejects it on the next turn.
- **Conversation ID:** memory is keyed per user with `.advisors(a -> a.param(ChatMemory.CONVERSATION_ID, username))` in the controller.
- **Tools** are `@Component` classes with `@Tool` methods in the `rag` package. `TimeTool` is registered with `defaultTools`. `HelpDeskTicketTool` is passed per request with `.tools(...)`, and receives the caller through `.toolContext(Map.of("username", username))`. Tool methods read `username` from `ToolContext` and must only act on that user's tickets. The model never supplies the username.
- **Help-desk data path:** `HelpDeskTicketTool` → `HelpDeskTicketService` → `HelpDeskTicketRepository` (JPA) → MySQL `HelpDeskTicket` entity (`Status`: OPEN / IN_PROGRESS / CLOSED; ETA is created time + 5 days).
- **Web-search RAG:** `WebSearchDocumentRetriever` is a custom `DocumentRetriever` that calls Tavily and maps results to `Document`s with `title`/`url` metadata. Its static `format` method is the `documentFormatter` for `ContextualQueryAugmenter`.
- **Semantic cache:** `SemanticCacheConfig` wraps the embedding model so that `dimensions()` returns the configured 768. The Gemini model otherwise reports 3072, and the Redis index would be created with the wrong size. Keep this wrapper if you change embedding settings, and drop the `video-script-cache` Redis index if the dimensions change.
- **Vector store seeding:** on startup, `RandomDataLoader` (`@PostConstruct`) embeds a fixed list of sentences into the Qdrant collection `spring-ai`. It skips this if the collection already has points, and uses deterministic UUIDs, so restarts don't duplicate data. If the embedding model or dimensions change, the Qdrant collection must be dropped (`docker compose down -v` removes the `qdrant_data` volume).
- **Prompts** live in `src/main/resources/promptTemplate/*.st` and are injected as `Resource` with `@Value("classpath:/promptTemplate/...")`. Templates passed to `ContextualQueryAugmenter` must contain the `{context}` and `{query}` placeholders.
- `TokenUsageAuditAdvisor` (in `advisor/`) is a custom `CallAdvisor` + `StreamAdvisor` that logs token usage. For streams it aggregates the chunks first. Advisor DEBUG logging is enabled for `SimpleLoggerAdvisor`.
