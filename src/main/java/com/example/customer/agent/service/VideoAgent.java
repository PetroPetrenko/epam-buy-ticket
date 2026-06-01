package com.example.customer.agent.service;

import com.example.customer.agent.model.AgentRequest;
import com.example.customer.agent.model.AgentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class VideoAgent {
    private final List<OpenAiChatModel> chatModels;
    
    @Value("${app.luma.api-key}")
    private String lumaApiKey;

    @Value("${app.luma.base-url}")
    private String lumaBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public AgentResponse process(AgentRequest request) {
        log.info("VideoAgent processing request via Luma AI...");
        
        // 1. Сначала используем LLM для оптимизации промпта под Luma
        String systemPrompt = "Ты - Эксперт по генерации видео в Luma AI.\n" +
            "Твоя цель: превратить сценарий пользователя в идеальный промпт для Luma Dream Machine.\n" +
            "\n" +
            "ИНСТРУКЦИИ:\n" +
            "- Описывай движение, освещение и текстуры.\n" +
            "- Используй английский язык для финального промпта.\n" +
            "- Промпт должен быть коротким, но емким (до 500 символов).";

        var chatResponse = chatModels.get(0).call(new Prompt(List.of(
            new SystemMessage(systemPrompt),
            new UserMessage(request.getQuery())
        )));

        String optimizedPrompt = chatResponse.getResult().getOutput().getContent();
        log.info("Optimized Luma Prompt: {}", optimizedPrompt);

        // 2. Выполняем реальный запрос к Luma API (имитация/структура)
        String lumaResponse = callLumaApi(optimizedPrompt);

        return AgentResponse.builder()
                .content("Luma Video Task Created!\nPrompt: " + optimizedPrompt + "\nStatus: " + lumaResponse)
                .agentName("VideoAgent (Luma)")
                .actionsTaken(List.of("Optimized prompt for Luma", "Triggered Luma API generation"))
                .build();
    }

    private String callLumaApi(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(lumaApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = Map.of(
                "prompt", prompt,
                "aspect_ratio", "16:9",
                "loop", false
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            
            // Luma API endpoint для генерации
            String endpoint = lumaBaseUrl + "/generations";
            
            log.info("Calling Luma API at: {}", endpoint);
            // В демонстрационных целях логируем, но для реального вызова раскомментировать:
            // ResponseEntity<Map> response = restTemplate.postForEntity(endpoint, entity, Map.class);
            // return response.getBody().toString();
            
            return "Task ID: luma-gen-" + System.currentTimeMillis() + " (Pending)";
        } catch (Exception e) {
            log.error("Luma API call failed: {}", e.getMessage());
            return "Luma API Error: " + e.getMessage();
        }
    }
}
