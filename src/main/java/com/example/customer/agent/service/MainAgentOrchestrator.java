package com.example.customer.agent.service;

import com.example.customer.agent.model.AgentRequest;
import com.example.customer.agent.model.AgentResponse;
import com.example.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Главный Агент (Orchestrator).
 * Координирует выполнение задач, использует RAG для контекста
 * и вызывает специализированные навыки.
 */
@Service
@Slf4j
public class MainAgentOrchestrator {

    private final List<OpenAiChatModel> chatModels;
    private final CustomerService customerService;
    private final FlightAgent flightAgent;
    private final HotelAgent hotelAgent;
    private final ScriptAgent scriptAgent;
    private final ImageAgent imageAgent;
    private final VideoAgent videoAgent;
    private final MotionDesignAgent motionAgent;
    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public MainAgentOrchestrator(
            List<OpenAiChatModel> chatModels,
            CustomerService customerService,
            FlightAgent flightAgent,
            HotelAgent hotelAgent,
            ScriptAgent scriptAgent,
            ImageAgent imageAgent,
            VideoAgent videoAgent,
            MotionDesignAgent motionAgent,
            ChatMemory chatMemory,
            ChatClient.Builder chatClientBuilder,
            @org.springframework.beans.factory.annotation.Autowired(required = false) VectorStore vectorStore) {
        this.chatModels = chatModels;
        this.customerService = customerService;
        this.flightAgent = flightAgent;
        this.hotelAgent = hotelAgent;
        this.scriptAgent = scriptAgent;
        this.imageAgent = imageAgent;
        this.videoAgent = videoAgent;
        this.motionAgent = motionAgent;
        this.vectorStore = vectorStore;
        
        // Создаем ChatClient с поддержкой памяти
        this.chatClient = chatClientBuilder
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
                .build();
    }

    private String fetchRagContext1(AgentRequest request) {
        if (vectorStore == null) {
            log.warn("VectorStore is not available. Skipping RAG context fetch.");
            return "No RAG context available.";
        }
        try {
            var results = vectorStore.similaritySearch(
                    SearchRequest.query(request.getQuery()).withTopK(3)
            );
            return results.stream()
                    .map(doc -> doc.getContent())
                    .reduce((a, b) -> a + "\n" + b)
                    .orElse("No relevant context found.");
        } catch (Exception e) {
            log.error("RAG fetch failed: {}", e.getMessage());
            return "Error fetching RAG context.";
        }
    }

    public AgentResponse handleRequest(AgentRequest request) {
        log.info("Main Agent received request: {}", request.getQuery());

        // 0. Обогащаем запрос данными клиента из БД, если передан customerId
        if (request.getCustomerId() != null && (request.getName() == null || request.getEmail() == null)) {
            try {
                var customer = customerService.findById(request.getCustomerId());
                if (request.getName() == null) request.setName(customer.name());
                if (request.getEmail() == null) request.setEmail(customer.email());
                if (request.getPhone() == null) request.setPhone(customer.phone());
                log.info("Enriched request with customer data: {} ({})", request.getName(), request.getEmail());
            } catch (Exception e) {
                log.warn("Could not enrich request with customer data: {}", e.getMessage());
            }
        }

        String systemPrompt = "";
        StringBuilder combinedResponse = new StringBuilder();
        List<String> allActions = new ArrayList<>();

        try {
            // 1. RAG: Получаем контекст клиента и правила через векторный поиск
            String ragContext = fetchRagContext(request);
            
            // 2. Формируем системный промпт
            systemPrompt = String.format("You are the Main Agent (Orchestrator) of the Media & Travel MAS system.\n" +
                "Your task: analyze user request and coordinate specialized agents.\n" +
                "\n" +
                "CONTEXT (RAG & PREVIOUS ACTIONS):\n" +
                "%s\n" +
                "\n" +
                "Your subordinate agents:\n" +
                "- Travel Group: FlightAgent, HotelAgent.\n" +
                "- Media Group: ScriptAgent, ImageAgent, VideoAgent, MotionDesignAgent.\n" +
                "\n" +
                "CHAINING INSTRUCTIONS:\n" +
                "1. If the user orders a ticket (Flight/Fly), always suggest booking a hotel in the DESTINATION city.\n" +
                "2. When passing a task to HotelAgent, always specify the city where the user is arriving so the hotel is selected there.\n" +
                "3. If the request concerns video/script creation, use the Media Group.\n" +
                "4. For complex media requests, run the chain: Script -> Image -> Video -> Motion.", ragContext);

            // 3. Логика маршрутизации
            String queryLower = request.getQuery().toLowerCase();
            // Media Pipeline
            if (queryLower.contains("сценарий") || queryLower.contains("видео") || queryLower.contains("ролик") || queryLower.contains("script") || queryLower.contains("video")) {
                log.info("Starting Media Production Pipeline...");
                
                AgentResponse scriptRes = scriptAgent.process(request);
                combinedResponse.append("🎬 Script Agent: ").append(scriptRes.getContent()).append("\n\n");
                allActions.addAll(scriptRes.getActionsTaken());

                AgentResponse imageRes = imageAgent.process(request);
                combinedResponse.append("🖼️ Image Agent: ").append(imageRes.getContent()).append("\n\n");
                allActions.addAll(imageRes.getActionsTaken());

                if (queryLower.contains("видео") || queryLower.contains("video")) {
                    AgentResponse videoRes = videoAgent.process(request);
                    combinedResponse.append("📹 Video Agent: ").append(videoRes.getContent()).append("\n\n");
                    allActions.addAll(videoRes.getActionsTaken());

                    AgentResponse motionRes = motionAgent.process(request);
                    combinedResponse.append("✨ Motion Design Agent: ").append(motionRes.getContent()).append("\n\n");
                    allActions.addAll(motionRes.getActionsTaken());
                }
            }

            // Travel Group
        if (queryLower.contains("flight") || queryLower.contains("fly") || queryLower.contains("билет") || queryLower.contains("лечу") || queryLower.contains("booking")) {
            AgentResponse flightRes = flightAgent.process(request);
            combinedResponse.append("✈️ Flight Agent: ").append(flightRes.getContent()).append("\n\n");
            allActions.addAll(flightRes.getActionsTaken());
            
            // Dynamic extraction of destination city for HotelAgent trigger
            String city = "this city";
            java.util.regex.Matcher cityMatcher = java.util.regex.Pattern.compile("(?i)(?:to|in|into|в|в г\\.)\\s+([A-ZА-Я][a-zа-я]+(?:\\s+[A-ZА-Я][a-zа-я]+)*?)(?=\\s+(?:on|at|from|to|with|by|in|$)|[\\.,!]|$)").matcher(request.getQuery());
            if (cityMatcher.find()) {
                city = cityMatcher.group(1).trim();
            }

            // If a flight is booked, automatically ask HotelAgent to find a hotel in that city
            log.info("Auto-triggering HotelAgent based on flight destination: {}", city);
            AgentResponse hotelRes = hotelAgent.process(request);
            combinedResponse.append("🏨 Hotel Agent (Auto): ").append(hotelRes.getContent())
                .append("\n(Automatically selected hotel in ").append(city).append(")\n\n");
            allActions.addAll(hotelRes.getActionsTaken());
        } else if (queryLower.contains("hotel") || queryLower.contains("отель") || queryLower.contains("гостиница")) {
            AgentResponse hotelRes = hotelAgent.process(request);
            combinedResponse.append("🏨 Hotel Agent: ").append(hotelRes.getContent()).append("\n\n");
            allActions.addAll(hotelRes.getActionsTaken());
        }

            if (combinedResponse.isEmpty()) {
                if (chatModels.isEmpty()) {
                    combinedResponse.append("Error: AI System not configured. Please add API keys in application.properties.");
                } else {
                    // Используем современный ChatClient с Advisors
                    String content = chatClient.prompt()
                            .system(systemPrompt)
                            .user(request.getQuery())
                            .advisors(a -> a.param(MessageChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, 
                                                request.getCustomerId() != null ? request.getCustomerId().toString() : "default"))
                            .call()
                            .content();
                    
                    combinedResponse.append(content);
                    allActions.add("Handled by ChatClient with Memory Advisor");
                }
            }
        } catch (Exception e) {
            log.error("Critical error in Orchestrator: {}", e.getMessage(), e);
            combinedResponse.append("Произошла ошибка при обработке запроса: ").append(e.getMessage());
            allActions.add("Error: " + e.getClass().getSimpleName());
        }

        return AgentResponse.builder()
                .content(combinedResponse.toString().trim())
                .agentName("MainOrchestrator")
                .actionsTaken(allActions)
                .build();
    }

    /**
     * Реальный RAG через VectorStore.
     */
    private String fetchRagContext(AgentRequest request) {
        StringBuilder context = new StringBuilder();
        
        // 1. Поиск по базе знаний
        try {
            if (vectorStore != null) {
                var docs = vectorStore.similaritySearch(
                    SearchRequest.query(request.getQuery()).withTopK(3)
                );
                
                if (!docs.isEmpty()) {
                    context.append("Релевантная информация из базы знаний:\n");
                    docs.forEach(doc -> context.append("- ").append(doc.getContent()).append("\n"));
                }
            } else {
                log.warn("VectorStore is not available, skipping knowledge base search");
            }
        } catch (Exception e) {
            log.error("Error searching vector store: {}", e.getMessage());
        }

        // 2. Контекст конкретного клиента (если есть ID)
        if (request.getCustomerId() != null) {
            try {
                var customer = customerService.findById(request.getCustomerId());
                context.append("\nДанные клиента:\n");
                context.append("Имя: ").append(customer.name()).append("\n");
                context.append("Заметки: ").append(customer.notes()).append("\n");
            } catch (Exception e) {
                log.warn("Customer not found or error fetching customer: {}", e.getMessage());
                context.append("\nДанные клиента: Информация недоступна (ID: ").append(request.getCustomerId()).append(")\n");
            }
        }
        
        return context.toString().isEmpty() ? "Контекст не найден." : context.toString();
    }
}
